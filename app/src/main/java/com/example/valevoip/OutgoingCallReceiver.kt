package com.example.valevoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.valevoip.main.presentation.MainActivity

class OutgoingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        var phoneNumber = resultData
        if (phoneNumber == null) {
            phoneNumber = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        }

        resultData = null

        context?.let {
            val intentCall = Intent(it, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("OUTBOUND_NUMBER", phoneNumber)
            }
            it.startActivity(intentCall)
        }

    }
}