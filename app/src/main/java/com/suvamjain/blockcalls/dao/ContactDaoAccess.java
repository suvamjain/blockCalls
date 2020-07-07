package com.suvamjain.blockcalls.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

@Dao
public interface ContactDaoAccess {

    @Insert
    Long insertContact(Contact contact);


    @Query("SELECT * FROM Contact")
    LiveData<List<Contact>> fetchAllContacts();


    @Query("SELECT * FROM Contact WHERE id =:contactId")
    LiveData<Contact> getContact(int contactId);


    @Query("SELECT * FROM Contact WHERE number =:phNumber")
    Contact getContactByNumber(String phNumber);


    @Update
    void updateContact(Contact contact);


    @Delete
    void deleteContact(Contact contact);


    @Query("DELETE FROM Contact")
    void deleteAllContacts();
}
