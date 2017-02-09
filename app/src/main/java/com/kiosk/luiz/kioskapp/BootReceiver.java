//
//  BootReceiver.java
//
//  Created by Sandro Albert on 2016/07/11.
//  Copyright (c) Sandro Albert. All rights reserved.
//


package com.kiosk.luiz.kioskapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

//        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB || chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {

        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.setAction(Intent.ACTION_MAIN);
        myIntent.addCategory(Intent.CATEGORY_HOME);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);

//        } else {
//            Toast.makeText(context, "Power is not plug in now, please be plug in!", Toast.LENGTH_LONG).show();
//        }
    }
}