package com.bitinovus.timerservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "TimerChannel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_DURATION = "DURATION"
    }

    private var job: Job? = null
    private var remainingTime = 0
    private var notificationManager: NotificationManager? = null


    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            TimerActions.START.toString() -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, 10) // 10 is default minutes time
                Log.d("INTENT", "onStartCommand: $duration")
                startTimer(duration)
            }

            TimerActions.STOP.toString() -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTimer(duration: Int) {
        timer(duration)
        startForeground(NOTIFICATION_ID, timerNotification(remainingTime))
    }

    private fun timerNotification(remainingTime: Int): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer")
            .setContentText("Time Left ${formater(remainingTime)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    }


    private fun timer(duration: Int) {
        remainingTime = duration * 60 // minute so seconds

        job = CoroutineScope(Dispatchers.Main).launch {
            while (remainingTime > 0) {
                delay(1000) // wait for 1 second
                remainingTime--
                updateTimerNotification(remainingTime)
            }
            stopSelf() // stop timer
        }
    }

    private fun notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateTimerNotification(remainingTime: Int) {
        notificationManager?.notify(NOTIFICATION_ID, timerNotification(remainingTime))
    }

    private fun formater(seconds: Int): String {
        val minutes = seconds / 60
        val sec = seconds % 60

        return String.format("%02d:%02d", minutes, sec)
    }


    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    enum class TimerActions {
        START, STOP
    }
}