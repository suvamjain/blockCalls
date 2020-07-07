package com.suvamjain.blockcalls.repository;

import android.arch.lifecycle.LiveData;

import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

public interface ContactRepository {

    void insertContact(String name, String number);

    void insertContact(final Contact contact);

    void updateContact(final Contact contact);

    void deleteContact(final int id);

    void deleteContact(final Contact contact);

    void deleteAllContacts();

    LiveData<Contact> getContact(int id);

    Contact getContactByNumber(String phNumber);

    LiveData<List<Contact>> getContacts();

}
