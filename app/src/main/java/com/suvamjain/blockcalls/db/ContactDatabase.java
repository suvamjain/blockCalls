package com.suvamjain.blockcalls.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.suvamjain.blockcalls.dao.ContactDaoAccess;
import com.suvamjain.blockcalls.model.Contact;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactDatabase extends RoomDatabase {

    public abstract ContactDaoAccess contactDaoAccess();
}
