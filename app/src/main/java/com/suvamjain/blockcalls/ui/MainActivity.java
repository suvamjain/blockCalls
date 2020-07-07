package com.suvamjain.blockcalls.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.suvamjain.blockcalls.AppConstants;
import com.suvamjain.blockcalls.R;
import com.suvamjain.blockcalls.adapter.BlockedContactListAdapter;
import com.suvamjain.blockcalls.model.Contact;
import com.suvamjain.blockcalls.repository.ContactRepository;
import com.suvamjain.blockcalls.util.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.MODIFY_PHONE_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_BOOT_COMPLETED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RecyclerItemClickListener.OnRecyclerViewItemClickListener, AppConstants {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = MainActivity.class.getName();

    private TextView emptyView;
    private RecyclerView recyclerView;
    private BlockedContactListAdapter blockedContactListAdapter;
    private FloatingActionButton floatingActionButton;

    private List<Contact> mContacts;
    private ContactRepository contactRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactRepository = new ContactRepository(getApplicationContext());
        mContacts = new ArrayList<>();

        recyclerView = findViewById(R.id.contact_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        emptyView = findViewById(R.id.empty_view);

        requestPermission();

        updateContactList();
    }

    private void updateContactList() {
        contactRepository.getContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                if(contacts.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    mContacts.clear();
                    mContacts.addAll(contacts);
                    if(blockedContactListAdapter == null) {
                        blockedContactListAdapter = new BlockedContactListAdapter(contacts);
                        recyclerView.setAdapter(blockedContactListAdapter);
                    }
                    else {
                        blockedContactListAdapter.addContacts(contacts);
                    }
                }
                else {
                    updateEmptyView();
                    mContacts.clear();
                }
            }
        });
    }

    private void updateEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE, READ_PHONE_STATE, MODIFY_PHONE_STATE, RECEIVE_BOOT_COMPLETED},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean callPhone = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readPhoneState = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (callPhone && readPhoneState)
                       Log.d(TAG,"Permissions Granted !!");
                    else
                    {
                        Log.d(TAG,"Permissions Not Granted !!");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{CALL_PHONE, READ_PHONE_STATE, MODIFY_PHONE_STATE, RECEIVE_BOOT_COMPLETED}, PERMISSION_REQUEST_CODE);
                                            }
                                        }
                                    });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /*
     * New contact to be added
     * */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    /*
     * update/delete existing contact
     * */
    @Override
    public void onItemClick(View parentView, View childView, int position) {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.putExtra(INTENT_CONTACT, blockedContactListAdapter.getItem(position));
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            //update or delete operation
            if (data.hasExtra(INTENT_CONTACT)) {
                Contact ct = (Contact) data.getSerializableExtra(INTENT_CONTACT);
                if (data.hasExtra(INTENT_DELETE)) {
                    contactRepository.deleteContact(ct);
                } else {
                    contactRepository.updateContact(ct);
                }
            }
            //insert operation
            else {
                final String name = data.getStringExtra(INTENT_NAME);
                final String number = data.getStringExtra(INTENT_NUMBER);
                new AsyncTask<Void, Void, Contact>() {
                    @Override
                    protected Contact doInBackground(Void... voids) {
                        return contactRepository.getContactByNumber(number);
                    }

                    @Override
                    protected void onPostExecute(Contact contact) {
                        super.onPostExecute(contact);
                        if (contact == null) {
                            contactRepository.insertContact(name, number);
                        } else {
                            Log.e(TAG, "Results --->" + contact.toString());
                            Toast.makeText(MainActivity.this, "Number already in block list", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
                updateContactList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            case R.id.action_clear: {
                contactRepository.deleteAllContacts();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
