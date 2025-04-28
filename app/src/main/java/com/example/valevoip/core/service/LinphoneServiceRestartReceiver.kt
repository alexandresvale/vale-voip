package com.example.valevoip.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LinphoneServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context, CallService::class.java).also {
            it.action = CallService.Actions.START.toString()
        }
        TODO("Not yet implemented")
    }

    private fun logEvent(string: String) {
        Log.d("ALE", "CallService | $string")
    }
}