package gpster.dev

import android.app.Application

class App : Application() {
    companion object {
        const val CHANNEL_ID : String = "gpster"
        const val NOTIFICATION_ID : Int = 1

        var isRunning : Boolean = false
            set(value) { field = value }
        var isRunningOverlay : Boolean = false
            set(value) { field = value }

        var lat : Double = 0.0
            set(value) { field = value }
        var lon : Double = 0.0
            set(value) { field = value }
        var alt : Double = 0.0
            set(value) { field = value }
        var speed : Double = 0.0
            set(value) { field = value }
    }
}