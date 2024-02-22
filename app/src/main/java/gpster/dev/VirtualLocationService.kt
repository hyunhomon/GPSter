package gpster.dev

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class VirtualLocationService : Service() {
    private lateinit var handler : Handler
    private lateinit var locationProvider : MyLocationProvider

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()

        handler = Handler(Looper.getMainLooper())
        locationProvider = MyLocationProvider(LocationManager.GPS_PROVIDER, this@VirtualLocationService)
        App.isRunning = true

        handler.postDelayed(updateRunnable, 100)

        return START_STICKY
    }

    private fun createNotification() {
        val mainIntent = Intent(this@VirtualLocationService, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@VirtualLocationService, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification : Notification = NotificationCompat.Builder(this@VirtualLocationService, App.CHANNEL_ID)
            .setContentTitle("가상 위치 서비스가 실행 중입니다")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(App.NOTIFICATION_ID, notification)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            locationProvider.setLocation(
                App.lat, App.lon, App.alt
            )
            handler.postDelayed(this, 100)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        locationProvider.shutdown()
        App.isRunning = false
    }
}