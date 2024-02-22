package gpster.dev

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import gpster.dev.databinding.ServiceOverlayBinding

class OverlayService : Service() {
    private var mBinding : ServiceOverlayBinding ?= null
    private val binding : ServiceOverlayBinding get() = requireNotNull(mBinding)

    private lateinit var wm : WindowManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mBinding = ServiceOverlayBinding.inflate(LayoutInflater.from(this@OverlayService))
        wm = this@OverlayService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        App.isRunningOverlay = true

        createNotification()
        createOverlay()

        return START_STICKY
    }

    private fun createNotification() {
        val mainIntent = Intent(this@OverlayService, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@OverlayService, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification : Notification = NotificationCompat.Builder(this@OverlayService, App.CHANNEL_ID)
            .setContentTitle("가상 위치 서비스가 실행 중입니다")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(App.NOTIFICATION_ID, notification)
    }

    private fun createOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.y = 320
        wm.addView(binding.root, params)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(binding.root)
        mBinding = null
        App.isRunningOverlay = false
    }
}