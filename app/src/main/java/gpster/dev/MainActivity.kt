package gpster.dev

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import gpster.dev.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding ?= null
    private val binding : ActivityMainBinding get() = requireNotNull(mBinding)

    private lateinit var providerName : String
    private lateinit var locationProvider : MyLocationProvider
    private lateinit var locationChecker: MyLocationChecker
    private lateinit var locationListener: MyLocationListener
    private lateinit var utilityProvider: UtilityProvider

    private var isRunning = false
    private var isExpanded = true
    private var isEditOpen = false

    private var lat = 00.0000
    private var lon = 00.0000
    private var alt = 00.0000
    private var speed = 10.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationChecker = MyLocationChecker(this)

        if(locationChecker.isLocationEnabled()) {
            if(!locationChecker.checkLocationPermission())
                locationChecker.requestLocationPermission(this)
        } else {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            this.startActivity(settingsIntent)
        }

        providerName = LocationManager.GPS_PROVIDER
        locationProvider = MyLocationProvider(providerName, this)
        locationListener = MyLocationListener()
        locationProvider.updateLocation(locationListener)
        utilityProvider = UtilityProvider(this)

        utilityProvider.hideKeyboard(this)
    }

    private fun setupEt() {
        utilityProvider.apply {
            with(binding) {
                if(isExpanded) {
                    focusHandling(etLat, etLon)
                    focusHandling(etLon, etAlt)
                    focusHandling(etAlt, etSpeed)
                } else {
                    focusHandling(etLat, etLoc0)
                    focusHandling(etLoc0, etLoc1)
                    focusHandling(etLoc1, etSpeed)
                }
                focusHandling(etSpeed, null)
            }
        }
    }

    private fun locationInfo() {
        if(isExpanded) {
            binding.apply {
                tvLat.setText(lat.toString())
                tvLon.setText(lon.toString())
                tvAlt.setText(alt.toString())
            }
        } else {
            binding.tvLat.setText("${lat}, ${lon}, ${alt}")
        }
        binding.tvSpeed.setText("${speed} km/h")
    }

    private fun locationInfoZoom() {
        binding.ivZoom.setOnClickListener() {
            if(!isExpanded) {
                binding.apply {
                    ivZoom.setImageResource(R.drawable.ic_zoom_in)
                    tvLocation.setText("위도:")
                    llLon.visibility = View.VISIBLE
                    llAlt.visibility = View.VISIBLE

                    isExpanded = true
                }
            } else {
                binding.apply {
                    ivZoom.setImageResource(R.drawable.ic_zoom_out)
                    tvLocation.setText("위치:")
                    llLon.visibility = View.GONE
                    llAlt.visibility = View.GONE

                    isExpanded = false
                }
            }
            locationInfo()
        }
    }

    private fun locationInfoEdit() {
        setupEt()
        binding.ivEdit.setOnClickListener() {
            if(!isEditOpen) {
                if(!isExpanded) {
                    binding.apply {
                        tvLat.visibility = View.GONE
                        etLat.visibility = View.VISIBLE
                        etLoc0.visibility = View.VISIBLE
                        etLoc1.visibility = View.VISIBLE
                    }
                } else {
                    binding.apply {
                        tvLat.visibility = View.GONE
                        tvLon.visibility = View.GONE
                        tvAlt.visibility = View.GONE
                        etLat.visibility = View.VISIBLE
                        etLon.visibility = View.VISIBLE
                        etAlt.visibility = View.VISIBLE
                    }
                }

                binding.apply {
                    tvSpeed.visibility = View.GONE
                    etSpeed.visibility = View.VISIBLE
                    ivEdit.setImageResource(R.drawable.ic_check)
                }
                isEditOpen = true
            } else {
                // et value gps reflection

                if(!isExpanded) {
                    binding.apply {
                        tvLat.visibility = View.VISIBLE
                        etLat.visibility = View.GONE
                        etLoc0.visibility = View.GONE
                        etLoc1.visibility = View.GONE
                    }
                } else {
                    binding.apply {
                        tvLat.visibility = View.VISIBLE
                        tvLon.visibility = View.VISIBLE
                        tvAlt.visibility = View.VISIBLE
                        etLat.visibility = View.GONE
                        etLon.visibility = View.GONE
                        etAlt.visibility = View.GONE
                    }
                }

                binding.apply {
                    tvSpeed.visibility = View.VISIBLE
                    etSpeed.visibility = View.GONE
                    ivEdit.setImageResource(R.drawable.ic_edit)
                }
                isEditOpen = false
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun btnOverlay() {
        binding.btnOverlay.setOnClickListener() {
            if(!isRunning) {
                // overlay start
                binding.btnOverlay.apply {
                    setText("종료")
                    setTextColor(R.color.blue)
                    setBackgroundResource(R.drawable.bg_btn_overlay_end)
                }

                isRunning = true
            } else {
                // overlay end
                binding.btnOverlay.apply {
                    setText("시작")
                    setTextColor(R.color.white)
                    setBackgroundResource(R.drawable.bg_btn_overlay_start)
                }

                isRunning = false
            }
        }
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(loc: Location) {
            lat = loc.latitude
            lon = loc.longitude
            alt = loc.altitude

            locationInfo()
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            // toast msg, shut down
        }
    }

    override fun onStart() {
        super.onStart()

        locationInfoZoom()
        locationInfoEdit()
        btnOverlay()
    }

    override fun onResume() {
        super.onResume()
        if(!locationChecker.isLocationEnabled() && !locationChecker.checkLocationPermission())
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}