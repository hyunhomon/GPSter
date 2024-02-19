package gpster.dev

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VirtualLocationService : Service() {
    private lateinit var locationProvider : MyLocationProvider

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        locationProvider.shutdown()
    }
}