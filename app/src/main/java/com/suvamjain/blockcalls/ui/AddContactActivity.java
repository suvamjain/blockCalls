package com.suvamjain.blockcalls.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.suvamjain.blockcalls.AppConstants;
import com.suvamjain.blockcalls.R;
import com.suvamjain.blockcalls.model.Contact;
import com.suvamjain.blockcalls.util.AppUtils;

public class AddContactActivity extends AppCompatActivity implements View.OnClickListener, AppConstants {

    private EditText editName, editNumber;
    private TextView btnDone, toolbarTitle;
    private ImageView btnDelete;

    private FloatingActionButton selectFromContact;

    private Contact contact;

    public static final int PICK_CONTACT = 100;

    private static final String[] phoneProjection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        toolbarTitle = findViewById(R.id.title);
        editName = findViewById(R.id.edit_name);
        editNumber = findViewById(R.id.edit_number);

        btnDelete = findViewById(R.id.btn_close);
        btnDelete.setOnClickListener(this);

        btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);

        selectFromContact = findViewById(R.id.select_contact);
        selectFromContact.setOnClickListener(this);

        contact = (Contact) getIntent().getSerializableExtra(INTENT_CONTACT);

        //when inserting a new contact
        if(contact == null) {
            toolbarTitle.setText(getString(R.string.add_contact));
            btnDelete.setImageResource(R.drawable.ic_cancel);
            btnDelete.setTag(R.drawable.ic_cancel);
            selectFromContact.show();
        }
        //when updating a contact
        else {
            toolbarTitle.setText(getString(R.string.edit_contact));
            btnDelete.setImageResource(R.drawable.ic_delete);
            btnDelete.setTag(R.drawable.ic_delete);
            selectFromContact.hide();

            if(contact.getName() != null && !contact.getName().isEmpty()) {
                editName.setText(contact.getName());
                editName.setSelection(contact.getName().length());
            }
            if(contact.getNumber() != null && !contact.getNumber().isEmpty()) {
                editNumber.setText(contact.getNumber());
                editNumber.setSelection(contact.getNumber().length());
                //disable updating the number
                editNumber.setEnabled(false);
            }
        }

        AppUtils.openKeyboard(getApplicationContext());
    }

    @Override
    public void onClick(View view) {
        AppUtils.hideKeyboard(this);

        if(view == btnDelete) {
            if((Integer)btnDelete.getTag() == R.drawable.ic_cancel) {
                setResult(Activity.RESULT_CANCELED);
            }
            else {
                Intent intent = getIntent();
                intent.putExtra(INTENT_DELETE, true);
                intent.putExtra(INTENT_CONTACT, contact);
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }
        else if(view == btnDone) {

            if(TextUtils.isEmpty(editName.getText())){
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(editNumber.getText()) || editNumber.getText().length() != 10) {
                Toast.makeText(this, "Number should be of 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = getIntent();

            //updating a contact
            if(contact != null) {
                contact.setName(editName.getText().toString());
                contact.setNumber(editNumber.getText().toString());
                intent.putExtra(INTENT_CONTACT, contact);
            }
            //adding new contact
            else {
                intent.putExtra(INTENT_NAME, editName.getText().toString());
                intent.putExtra(INTENT_NUMBER, editNumber.getText().toString());
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }
        else if(view == selectFromContact) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);  //should filter only contacts with phone numbers
            startActivityForResult(intent, PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PICK_CONTACT != requestCode || RESULT_OK != resultCode) return;
        Uri contactUri = data.getData();
        if (null == contactUri) return;
        //Uri makes this to work without READ_CONTACTS permission
        Cursor cursor = getContentResolver().query(contactUri, phoneProjection, null, null, null);
        if (null == cursor) return;
        try {
            while (cursor.moveToNext()) {
                String number = cursor.getString(0).replaceAll("[^\\d]", "");
                if(number.length() > 10) {
                    number = number.substring(number.length() - 10);
                }
                String name = cursor.getString(1);

                Log.d("Number picked -- ", number + ", name " + name);

                editNumber.setText("" + number);
                if(number.length() <= 10)
                    editNumber.setSelection(number.length());
                editName.setText("" + name);
                editName.setSelection(name.length());
            }
        } finally {
            cursor.close();
        }
    }
}
