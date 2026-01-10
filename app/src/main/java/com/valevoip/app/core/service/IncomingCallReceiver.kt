package com.valevoip.app.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.sip.SipAudioCall
import com.valevoip.app.presentation.feature.main.MainActivity


class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var incomingCall: SipAudioCall? = null
        val activity = context as? MainActivity

    }
}