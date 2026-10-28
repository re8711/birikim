package com.rslsoft.birikim.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Telefon yeniden başlatıldığında WorkManager hatırlatıcılarını tekrar kurar.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderWorker.createNotificationChannel(context)
            ReminderWorker.schedulePeriodicCheck(context)
        }
    }
}
