package gpster.dev

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class VirtualLocationService : Service() {
//    private lateinit var locationProvider : MyLocationProvider

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this@VirtualLocationService, "location service started", Toast.LENGTH_SHORT).show()
        showNotification()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val nm = this@VirtualLocationService.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "gpster",
            "gpster",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        nm.createNotificationChannel(channel)
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

    override fun onDestroy() {
        super.onDestroy()
//        locationProvider.shutdown()
    }
}