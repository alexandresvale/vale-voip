package com.example.valevoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.sip.SipAudioCall
import com.example.valevoip.main.presentation.MainActivity


class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var incomingCall: SipAudioCall? = null
        val activity = context as? MainActivity

    }
}