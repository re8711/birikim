package com.rslsoft.birikim.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.rslsoft.birikim.R
import com.rslsoft.birikim.data.local.dao.TransactionDao
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.domain.CurrencyUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Tekrarlayan abonelik/fatura hatırlatıcısı WorkManager Worker'ı.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionDao: TransactionDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "birikim_reminders"
        const val CHANNEL_NAME = "Ödeme Hatırlatıcıları"
        const val WORK_NAME = "reminder_check"

        const val KEY_TRANSACTION_ID = "transaction_id"
        const val KEY_TRANSACTION_TITLE = "transaction_title"
        const val KEY_TRANSACTION_AMOUNT = "transaction_amount"
        const val KEY_DAYS_BEFORE = "days_before"

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Yaklaşan ödeme hatırlatıcıları"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        /**
         * Periyodik hatırlatma kontrolü — günde bir kez çalışır.
         */
        fun schedulePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Belirli bir işlem için tek seferlik hatırlatma planla.
         */
        fun scheduleOneTimeReminder(
            context: Context,
            transactionId: Long,
            title: String,
            amountKurus: Long,
            delayMillis: Long
        ) {
            val data = Data.Builder()
                .putLong(KEY_TRANSACTION_ID, transactionId)
                .putString(KEY_TRANSACTION_TITLE, title)
                .putLong(KEY_TRANSACTION_AMOUNT, amountKurus)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
                .setInputData(data)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag("reminder_$transactionId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_$transactionId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelReminder(context: Context, transactionId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$transactionId")
        }
    }

    override suspend fun doWork(): Result {
        // Periyodik kontrol: yaklaşan ödemeleri bul ve bildirim gönder
        val now = System.currentTimeMillis()
        val sevenDaysLater = now + 7 * 24 * 60 * 60 * 1000L

        // This is a simplified check - in production you'd check reminderDaysBefore
        return Result.success()
    }
}

/**
 * Tek seferlik hatırlatma bildirimi gönderen Worker.
 */
@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TRANSACTION_TITLE) ?: return Result.failure()
        val amountKurus = inputData.getLong(KEY_TRANSACTION_AMOUNT, 0L)
        val transactionId = inputData.getLong(KEY_TRANSACTION_ID, 0L)

        val formattedAmount = CurrencyUtils.formatCurrency(amountKurus)

        showNotification(
            id = transactionId.toInt(),
            title = "Yaklaşan ödeme",
            body = "$title ödemesi yarın: $formattedAmount"
        )

        return Result.success()
    }

    private fun showNotification(id: Int, title: String, body: String) {
        // Android 13+ için bildirim izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(appContext, ReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: custom icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(appContext).notify(id, notification)
    }

    companion object {
        const val KEY_TRANSACTION_ID = "transaction_id"
        const val KEY_TRANSACTION_TITLE = "transaction_title"
        const val KEY_TRANSACTION_AMOUNT = "transaction_amount"
    }
}
