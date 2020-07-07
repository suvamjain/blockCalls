package com.suvamjain.blockcalls;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.suvamjain.blockcalls.dao.ContactDaoAccess;
import com.suvamjain.blockcalls.db.ContactDatabase;
import com.suvamjain.blockcalls.repository.ContactDataSource;
import com.suvamjain.blockcalls.repository.ContactRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RoomModule {

    private ContactDatabase contactDatabase;
    private String DB_NAME="db_block_calls";

    public RoomModule(Application mApplication) {
        contactDatabase = Room.databaseBuilder(mApplication, ContactDatabase.class, DB_NAME).build();
    }

    @Singleton
    @Provides
    ContactDatabase providesRoomDatabase() {
        return contactDatabase;
    }

    @Singleton
    @Provides
    ContactDaoAccess providesContactDao(ContactDatabase contactDatabase) {
        return contactDatabase.contactDaoAccess();
    }

    @Singleton
    @Provides
    ContactRepository contactRepository(ContactDaoAccess contactDao) {
        return new ContactDataSource(contactDao);
    }

}