package com.suvamjain.blockcalls.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.suvamjain.blockcalls.dao.ContactDaoAccess;
import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

import javax.inject.Inject;

public class ContactDataSource implements ContactRepository {

    private ContactDaoAccess contactDaoAccess;

    @Inject
    public ContactDataSource(ContactDaoAccess contactDaoAccess) {
        this.contactDaoAccess = contactDaoAccess;
    }

    @Override
    public void insertContact(String name, String number) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setNumber(number);

        insertContact(contact);
    }

    @Override
    public void insertContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDaoAccess.insertContact(contact);
                return null;
            }
        }.execute();
    }

    @Override
    public void updateContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDaoAccess.updateContact(contact);
                return null;
            }
        }.execute();
    }

    @Override
    public void deleteContact(int id) {
        final LiveData<Contact> contact = getContact(id);
        if(contact != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    contactDaoAccess.deleteContact(contact.getValue());
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void deleteContact(final Contact contact) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDaoAccess.deleteContact(contact);
                return null;
            }
        }.execute();
    }

    @Override
    public void deleteAllContacts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contactDaoAccess.deleteAllContacts();
                return null;
            }
        }.execute();
    }

    @Override
    public LiveData<Contact> getContact(int id) {
        return contactDaoAccess.getContact(id);
    }

    @Override
    public Contact getContactByNumber(String phNumber) {
        return contactDaoAccess.getContactByNumber(phNumber);
    }

    @Override
    public LiveData<List<Contact>> getContacts() {
        return contactDaoAccess.fetchAllContacts();
    }
}
