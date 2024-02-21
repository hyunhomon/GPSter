package gpster.dev

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.app.Activity
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.lang.RuntimeException

class MyLocationProvider(
    private val providerName : String,
    private val context : Context
) {
    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        lm.addTestProvider(providerName, false, false, false, false, true, true, true,
            ProviderProperties.POWER_USAGE_HIGH, ProviderProperties.ACCURACY_FINE)
        lm.setTestProviderEnabled(providerName, true)
    }

    fun setLocation(lat: Double, lon: Double, alt: Double) {
        try {
            var mockLocation = Location(providerName)
            mockLocation.apply {
                latitude = lat
                longitude = lon
                altitude = alt
                accuracy = 1.0f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }

            lm.setTestProviderLocation(providerName, mockLocation)
        }
        catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        lm.removeTestProvider(providerName)
    }
}

class MyLocationChecker(
    private val context : Context
) {
    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun isLocationEnabled() : Boolean {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun checkLocationPermission() : Boolean {
        val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermissionGranted
    }

    fun requestLocationPermission(activity: Activity) {
        activity.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 0
        )
    }
}
