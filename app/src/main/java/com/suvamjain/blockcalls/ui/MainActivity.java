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
import com.suvamjain.blockcalls.AppModule;
import com.suvamjain.blockcalls.DaggerAppComponent;
import com.suvamjain.blockcalls.R;
import com.suvamjain.blockcalls.RoomModule;
import com.suvamjain.blockcalls.adapter.BlockedContactListAdapter;
import com.suvamjain.blockcalls.model.Contact;
import com.suvamjain.blockcalls.repository.ContactRepository;
import com.suvamjain.blockcalls.util.RecyclerItemClickListener;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.observers.SubscriberCompletableObserver;
import io.reactivex.schedulers.Schedulers;

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

    @Inject
    public ContactRepository contactRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DaggerAppComponent.builder()
                .appModule(new AppModule(getApplication()))
                .roomModule(new RoomModule(getApplication()))
                .build()
                .inject(this);

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
                    if(blockedContactListAdapter == null) {
                        blockedContactListAdapter = new BlockedContactListAdapter(getApplicationContext(), contacts);
                        recyclerView.setAdapter(blockedContactListAdapter);
                    }
                    else {
                        blockedContactListAdapter.addContacts(contacts);
                    }
                }
                else {
                    updateEmptyView();
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
                final Contact ct = (Contact) data.getSerializableExtra(INTENT_CONTACT);
                if (data.hasExtra(INTENT_DELETE)) {
                    //delete contact
                    contactRepository.deleteContact(ct)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                Log.e(TAG, "Deleted successfully -> " + integer);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error while deleting " + e.getMessage());
                            }
                        });
                }
                else {
                    //update contact
                    contactRepository.updateContact(ct)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                Log.e(TAG, "Updated successfully -> " + integer);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error while updating " + e.getMessage());
                            }
                        });
                }
            }
            //insert operation
            else {
                final String name = data.getStringExtra(INTENT_NAME);
                final String number = data.getStringExtra(INTENT_NUMBER);

                //insert contact if number not exists
                contactRepository.getContactByNumber(number)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Contact>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Contact contact) {
                            Log.e(TAG, "Error: Contact already exists with name -> " + contact.getName());
                            Toast.makeText(MainActivity.this, "Number already in block list", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Contact not exists, inserting user");
                            contactRepository.insertContact(name, number)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        Log.e(TAG, "Inserted successfully");
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.e(TAG, "Error while inserting");
                                    }
                                });
                        }
                    });
                updateContactList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                contactRepository.deleteAllContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, "Deleted -> " + integer + " contacts successfully");
                        Toast.makeText(MainActivity.this, "Deleted " + integer + " contacts successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error while deleting: " + e.getMessage());
                    }
                });
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
