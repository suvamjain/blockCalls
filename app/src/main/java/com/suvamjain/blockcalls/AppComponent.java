package com.suvamjain.blockcalls;

import android.app.Application;

import com.suvamjain.blockcalls.dao.ContactDaoAccess;
import com.suvamjain.blockcalls.db.ContactDatabase;
import com.suvamjain.blockcalls.receiver.IncomingCallReceiver;
import com.suvamjain.blockcalls.repository.ContactRepository;
import com.suvamjain.blockcalls.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(dependencies = {}, modules = {AppModule.class, RoomModule.class})
public interface AppComponent {

    void inject(MainActivity mainActivity);

    void injectReceiver(IncomingCallReceiver incomingCallReceiver);

    ContactDaoAccess contactDao();

    ContactDatabase contactDatabase();

    ContactRepository contactRepository();

    Application application();

}