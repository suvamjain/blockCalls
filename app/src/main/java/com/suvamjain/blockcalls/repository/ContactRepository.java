package com.suvamjain.blockcalls.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.suvamjain.blockcalls.db.ContactDatabase;
import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

public class ContactRepository {

    private String DB_NAME="db_block_calls";

    private ContactDatabase contactDatabase;

    public ContactRepository(Context context) {
        contactDatabase = Room.databaseBuilder(context, ContactDatabase.class, DB_NAME).build();
    }

    public void insertContact(String name, String number) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setNumber(number);
        
        insertContact(contact);
    }

    public void insertContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDatabase.contactDaoAccess().insertContact(contact);
                return null;
            }
        }.execute();
    }

    public void updateContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDatabase.contactDaoAccess().updateContact(contact);
                return null;
            }
        }.execute();
    }

    public void deleteContact(final int id) {
        final LiveData<Contact> contact = getContact(id);
        if(contact != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    contactDatabase.contactDaoAccess().deleteContact(contact.getValue());
                    return null;
                }
            }.execute();
        }
    }

    public void deleteContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDatabase.contactDaoAccess().deleteContact(contact);
                return null;
            }
        }.execute();
    }

    public void deleteAllContacts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDatabase.contactDaoAccess().deleteAllContacts();
                return null;
            }
        }.execute();
    }

    public LiveData<Contact> getContact(int id) {
        return contactDatabase.contactDaoAccess().getContact(id);
    }

    public Contact getContactByNumber(String phNumber) {
        return contactDatabase.contactDaoAccess().getContactByNumber(phNumber);
    }


    public LiveData<List<Contact>> getContacts() {
        return contactDatabase.contactDaoAccess().fetchAllContacts();
    }

}
