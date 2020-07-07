package com.suvamjain.blockcalls.repository;

import android.arch.lifecycle.LiveData;

import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface ContactRepository {

    Completable insertContact(String name, String number);

    Completable insertContact(Contact contact);

    Single<Integer> updateContact(Contact contact);

    Single<Integer> deleteContact(Contact contact);

    Single<Integer> deleteAllContacts();

    LiveData<Contact> getContact(int id);

    Single<Contact> getContactByNumber(String phNumber);

    Completable findAndUpdateCount(int id);

    LiveData<List<Contact>> getContacts();

}
