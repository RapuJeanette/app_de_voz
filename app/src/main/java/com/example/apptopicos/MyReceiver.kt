package com.example.apptopicos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val serviceIntent = Intent(context, MyService::class.java)
            context?.startForegroundService(serviceIntent)
        }
    }
}