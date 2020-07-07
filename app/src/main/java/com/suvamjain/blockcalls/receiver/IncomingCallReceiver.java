package com.suvamjain.blockcalls.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.suvamjain.blockcalls.AppModule;
import com.suvamjain.blockcalls.DaggerAppComponent;
import com.suvamjain.blockcalls.R;
import com.suvamjain.blockcalls.RoomModule;
import com.suvamjain.blockcalls.model.Contact;
import com.suvamjain.blockcalls.repository.ContactRepository;
import com.suvamjain.blockcalls.ui.MainActivity;

import java.lang.reflect.Method;

import javax.inject.Inject;

public class IncomingCallReceiver extends BroadcastReceiver {

    public IncomingCallReceiver() {}

    private static final String TAG = IncomingCallReceiver.class.getName();

    private String incomingNumber;
    private String state;
    private Contact person;

    private static final int NOTIFY_REJECTED = 0;

    @Inject
    public ContactRepository contactRepository;

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (!checkReadPhoneStatePermission(context) || !checkCallPhonePermission(context)) {
            return;
        }

        // get telephony service
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
            return;
        }

        //ignore the broadcasts where EXTRA_INCOMING_NUMBER is not present in the extras
        if (!intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
            Log.d(TAG, "Event had no incoming_number metadata. Letting it ring...");
            return;
        }

        // get incoming call number
        incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(TAG, "Incoming number: " + incomingNumber);

        DaggerAppComponent.builder()
                .appModule(new AppModule((Application)context.getApplicationContext()))
                .roomModule(new RoomModule((Application)context.getApplicationContext()))
                .build()
                .injectReceiver(this);

        new notifyAsyncTask().execute(context);
    }

    private class notifyAsyncTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... params) {
            Log.i(TAG, "Notify async started");
            Contact contact = contactRepository.getContactByNumber(incomingNumber.substring(3));
            if( contact != null) {
                breakCall(params[0], contact);
            }
            return null;
        }
    }

    private void breakCallNougatAndLower(Context context) {
        Log.d(TAG, "Trying to disconnect call for Nougat and lower with TelephonyManager.");
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(telephony);
            telephonyService.endCall();
            notifyUser(context);
        } catch (Exception e) {
            Log.e(TAG, "Could not end call");
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void breakCallPieAndHigher(Context context) {
        Log.d(TAG, "Trying to break call for Pie and higher with TelecomManager.");
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        try {
            telecomManager.getClass().getMethod("endCall").invoke(telecomManager);
            notifyUser(context);
        } catch (Exception e) {
            Log.e(TAG, "Could not end call");
            e.printStackTrace();
        }
    }

    // Ends phone call
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void breakCall(Context context, Contact contact) {

        //if permission not granted
        if (!checkCallPhonePermission(context)) {
            return;
        }

        person = contact;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            breakCallPieAndHigher(context);
        }
        else {
            breakCallNougatAndLower(context);
        }

    }

    private boolean checkCallPhonePermission(Context context) {
        return context.checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkReadPhoneStatePermission(Context context) {
        return context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void notifyUser(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                    context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.receiver_notify_call_rejected));
            notificationManager.createNotificationChannel(channel);
        }

        Notification notify = new NotificationCompat.Builder(context, "M_CH_ID")
                .setSmallIcon(R.drawable.ic_block_icon)
                .setColor(context.getResources().getColor(R.color.bg_lock))
                .setContentTitle(context.getString(R.string.receiver_notify_call_rejected))
                .setContentText(person != null ? person.getName() + " : " + person.getNumber(): context.getString(R.string.receiver_notify_private_number))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .addPerson("tel:" + person.getNumber())
                .setGroup("rejected")
                .setChannelId("default")
                .setGroupSummary(true)
                .build();

        String tag = person != null ? person.getNumber() : "private";
        NotificationManagerCompat.from(context).notify(tag, NOTIFY_REJECTED, notify);
    }
}
