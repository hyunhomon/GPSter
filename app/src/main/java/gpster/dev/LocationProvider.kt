package gpster.dev

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.app.Activity

import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock

import androidx.core.content.ContextCompat
import java.lang.RuntimeException


class LocationProvider(
    private val providerName : String,
    private val context : Context
) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    init {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.addTestProvider(providerName, true, true, true, true, true, true, true,
            ProviderProperties.POWER_USAGE_MEDIUM, ProviderProperties.ACCURACY_FINE)
        lm.setTestProviderEnabled(providerName, true)
    }

    fun isLocationEnabled() : Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun checkLocationPermission() : Boolean {
        val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermissionGranted && coarseLocationPermissionGranted
    }

    fun requestLocationPermission(activity: Activity) {
        activity.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun setLocation(lat: Double, lon: Double, alt: Double) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val mockLocation = Location(providerName)
            val currentTime = System.currentTimeMillis()

            mockLocation.apply {
                latitude = lat
                longitude = lon
                altitude = alt
                time = currentTime
                accuracy = 1.0f
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }

            lm.setTestProviderEnabled(providerName, true)
            lm.setTestProviderLocation(providerName, mockLocation)
        }
        catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

//    fun getLocation() : Location {
//        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return lm.getLastKnownLocation(providerName) ?: Location(providerName)
//    }

    fun shutdown() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.removeTestProvider(providerName)
    }
}
