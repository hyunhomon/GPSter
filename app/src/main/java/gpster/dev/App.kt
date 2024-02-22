package gpster.dev

import android.app.Application
import androidx.lifecycle.MutableLiveData

class App : Application() {
    companion object {
        var isRunning = MutableLiveData<Boolean>()
        var isRunningOverlay = MutableLiveData<Boolean>()

        var location = MutableLiveData<LocationModel>()
        var speed : Double = 0.0
            set(value) = run { field = value }
    }

    override fun onCreate() {
        super.onCreate()

        isRunning.postValue(false)
        isRunningOverlay.postValue(false)
        location.postValue(LocationModel(0.0, 0.0, 0.0))
    }
}