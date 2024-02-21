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
    private lateinit var app : App
    private lateinit var handler : Handler
    private lateinit var locationProvider : MyLocationProvider

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        app = application as App
        handler = Handler(Looper.getMainLooper())

        if(!app.isRunning) {
            showNotification()
            locationProvider = MyLocationProvider(LocationManager.GPS_PROVIDER, this@VirtualLocationService)
            app.isRunning = true
            handler.postDelayed(updateRunnable, 100)
        }

        return START_STICKY
    }

    private fun showNotification() {
        val mainIntent = Intent(this@VirtualLocationService, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@VirtualLocationService, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification : Notification = NotificationCompat.Builder(this@VirtualLocationService, "gpster")
            .setContentTitle("가상 위치 서비스가 실행 중입니다")
            .setSmallIcon(R.drawable.ic_teleport)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            locationProvider.setLocation(app.lat, app.lon, app.alt)
            handler.postDelayed(this, 100)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        locationProvider.shutdown()
    }
}