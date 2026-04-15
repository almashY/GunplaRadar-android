package com.example.gunplaradar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gunplaradar.R

class PatrolNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storeName = inputData.getString(KEY_STORE_NAME) ?: "店舗"
        val planDate = inputData.getString(KEY_PLAN_DATE) ?: ""
        createNotificationChannel()
        showNotification(storeName, planDate)
        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "巡回通知",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "ガンプラ巡回予定の通知"
        }
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(storeName: String, planDate: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ガンプラ巡回予定")
            .setContentText("$planDate に $storeName への巡回予定があります")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "patrol_notification_channel"
        const val KEY_STORE_NAME = "store_name"
        const val KEY_PLAN_DATE = "plan_date"
    }
}
