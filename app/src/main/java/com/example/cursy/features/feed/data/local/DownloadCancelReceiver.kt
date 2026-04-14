package com.example.cursy.features.feed.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.cursy.CANCEL_DOWNLOAD") {
            val courseId = intent.getStringExtra("courseId")
            // david fix: enviar stop al servicio existente, no crear uno nuevo
            val stopIntent = Intent(context, DownloadService::class.java).apply {
                action = DownloadService.ACTION_STOP
                putExtra("courseId", courseId)
            }
            context.stopService(stopIntent)  // stopService, no startForegroundService
        }
    }
}