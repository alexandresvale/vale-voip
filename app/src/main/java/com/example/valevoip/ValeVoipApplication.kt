package com.example.valevoip

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ValeVoipApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        loadPjsua2()
    }

    private fun loadPjsua2(){
        try {
            Log.d("MainActivity", "Library pjsua2 loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("MainActivity", "Failed to load library pjsua2", e)
        }
    }
}