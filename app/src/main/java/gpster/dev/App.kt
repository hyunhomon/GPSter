package gpster.dev

import android.app.Application

class App : Application() {
    var isRunning : Boolean = false
    var isRunningOverlay : Boolean = false

    var lat : Double = 0.0
    var lon : Double = 0.0
    var alt : Double = 0.0
    var speed : Double = 0.0
}