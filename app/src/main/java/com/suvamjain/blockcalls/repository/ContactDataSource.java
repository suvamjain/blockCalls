package com.suvamjain.blockcalls.repository;

import android.arch.lifecycle.LiveData;

import com.suvamjain.blockcalls.dao.ContactDaoAccess;
import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;

public class ContactDataSource implements ContactRepository {

    private ContactDaoAccess contactDaoAccess;

    @Inject
    public ContactDataSource(ContactDaoAccess contactDaoAccess) {
        this.contactDaoAccess = contactDaoAccess;
    }

    @Override
    public Completable insertContact(String name, String number) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setNumber(number);

        return insertContact(contact);
    }

    @Override
    public Completable insertContact(final Contact contact) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                contactDaoAccess.insertContact(contact);
            }
        });
    }

    @Override
    public Single<Integer> updateContact(final Contact contact) {
        return Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                int res = contactDaoAccess.updateContact(contact);
                if(res > 0)
                    emitter.onSuccess(res);
                else
                    emitter.onError(new Throwable("No contact updated"));
            }
        });
    }

    @Override
    public Single<Integer> deleteContact(final Contact contact) {
        return Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                int res = contactDaoAccess.deleteContact(contact);
                if(res > 0)
                    emitter.onSuccess(res);
                else
                    emitter.onError(new Throwable("No contact deleted"));
            }
        });
    }

    @Override
    public Single<Integer> deleteAllContacts() {
        return Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                int res = contactDaoAccess.deleteAllContacts();
                if(res > 0)
                    emitter.onSuccess(res);
                else
                    emitter.onError(new Throwable("No contacts deleted"));
            }
        });
    }

    @Override
    public LiveData<Contact> getContact(int id) {
        return contactDaoAccess.getContact(id);
    }

    @Override
    public Single<Contact> getContactByNumber(String phNumber) {
        return contactDaoAccess.getContactByNumber(phNumber);
    }

    @Override
    public Completable findAndUpdateCount(final int id) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                contactDaoAccess.findAndUpdateCount(id);
            }
        });
    }

    @Override
    public LiveData<List<Contact>> getContacts() {
        return contactDaoAccess.fetchAllContacts();
    }
}
