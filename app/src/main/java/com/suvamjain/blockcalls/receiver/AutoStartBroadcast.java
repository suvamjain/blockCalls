package com.suvamjain.blockcalls.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, IncomingCallReceiver.class);
        context.startService(i);
    }
}
