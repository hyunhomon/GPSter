package gpster.dev

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import gpster.dev.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding ?= null
    private val binding : ActivityMainBinding get() = requireNotNull(mBinding)

    private lateinit var providerName : String
    private lateinit var locationManager : LocationManager
    private lateinit var locationProvider : MyLocationProvider
    private lateinit var locationChecker : MyLocationChecker
    private lateinit var utilityProvider : UtilityProvider

    private var isRunning = false
    private var isExpanded = true
    private var isEditOpen = false

    private var lat = 0.0
    private var lon = 0.0
    private var alt = 0.0
    private var speed = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setup() {
        locationChecker = MyLocationChecker(this@MainActivity)

        if(locationChecker.isLocationEnabled()) {
            if(!locationChecker.checkLocationPermission())
                locationChecker.requestLocationPermission(this@MainActivity)
        } else {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            this@MainActivity.startActivity(settingsIntent)
        }

        providerName = LocationManager.GPS_PROVIDER
        locationManager = this@MainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        locationProvider = MyLocationProvider(providerName, this@MainActivity)
        utilityProvider = UtilityProvider(this@MainActivity)
        speed = utilityProvider.getSpeed()

        binding.root.setOnClickListener() {
            utilityProvider.hideKeyboard(this@MainActivity)
        }
        requestLocation()
    }

    private fun setupEt() {
        utilityProvider.apply {
            with(binding) {
                focusClear(listOf(
                    etLat, etLoc0, etLoc1, etLon, etAlt
                ))
                if(isExpanded) {
                    focusHandling(etLat, etLon, null)
                    focusHandling(etLon, etAlt, null)
                    focusHandling(etAlt, etSpeed, null)
                } else {
                    focusHandling(etLat, etLoc0, null)
                    focusHandling(etLoc0, etLoc1, null)
                    focusHandling(etLoc1, etSpeed, null)
                }
                focusHandling(etSpeed, null, this@MainActivity)
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
        binding.ivEdit.setOnClickListener() {
            setupEt()
            if(!isEditOpen) {
                binding.ivZoom.visibility = View.GONE
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
                utilityProvider.hideKeyboard(this@MainActivity)
                binding.ivZoom.visibility = View.VISIBLE

                lat = utilityProvider.parseToDouble(binding.etLat.text.toString(), -90.0, 90.0, lat)
                speed = utilityProvider.parseToDouble(binding.etSpeed.text.toString(), 0.0, 100.0, speed)

                if(!isExpanded) {
                    lon = utilityProvider.parseToDouble(binding.etLoc0.text.toString(), -180.0, 180.0, lon)
                    alt = utilityProvider.parseToDouble(binding.etLoc1.text.toString(), 0.0, 9999.9, alt)

                    binding.apply {
                        tvLat.visibility = View.VISIBLE
                        etLat.apply {
                            visibility = View.GONE
                            setText("")
                        }
                        etLoc0.apply {
                            visibility = View.GONE
                            setText("")
                        }
                        etLoc1.apply {
                            visibility = View.GONE
                            setText("")
                        }
                    }
                } else {
                    lon = utilityProvider.parseToDouble(binding.etLon.text.toString(), -180.0, 180.0, lon)
                    alt = utilityProvider.parseToDouble(binding.etAlt.text.toString(), 0.0, 9999.9, alt)

                    binding.apply {
                        tvLat.visibility = View.VISIBLE
                        tvLon.visibility = View.VISIBLE
                        tvAlt.visibility = View.VISIBLE
                        etLat.apply {
                            visibility = View.GONE
                            setText("")
                        }
                        etLon.apply {
                            visibility = View.GONE
                            setText("")
                        }
                        etAlt.apply {
                            visibility = View.GONE
                            setText("")
                        }
                    }
                }

                binding.apply {
                    tvSpeed.visibility = View.VISIBLE
                    etSpeed.apply {
                        visibility = View.GONE
                        setText("")
                    }
                    ivEdit.setImageResource(R.drawable.ic_edit)
                }
                isEditOpen = false

                locationInfo()
            }
        }
    }

    private fun locationSearch() {
        binding.llSearch.setOnClickListener() {
            binding.etSearch.requestFocus()
            utilityProvider.showKeyboard(this@MainActivity)
        }
        binding.ivSearch.setOnClickListener() {
            utilityProvider.hideKeyboard(this@MainActivity)
            binding.etSearch.clearFocus()

            searchLocation(binding.etSearch.text.toString())
        }
        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    utilityProvider.hideKeyboard(this@MainActivity)
                    binding.etSearch.clearFocus()

                    searchLocation(binding.etSearch.text.toString())

                    true
                }
                else -> false
            }
        })
    }

    private fun btnOverlay() {
        binding.btnOverlay.setOnClickListener() {
            if(!isRunning) {
                // overlay start
                binding.btnOverlay.apply {
                    setText("종료")
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
                    setBackgroundResource(R.drawable.bg_outlined_box)
                }

                isRunning = true
            } else {
                // overlay end
                binding.btnOverlay.apply {
                    setText("시작")
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    setBackgroundResource(R.drawable.bg_fill_box)
                }

                isRunning = false
            }
        }
    }

    private fun requestLocation() {
        if(locationChecker.checkLocationPermission()) {
            val lastKnownLocation : Location? = locationManager.getLastKnownLocation(providerName)
            lastKnownLocation?.let {
                lat = "%.1f".format(it.latitude).toDouble()
                lon = "%.1f".format(it.longitude).toDouble()
                alt = "%.1f".format(it.altitude).toDouble()
            }
            locationInfo()
        }
    }

    private fun searchLocation(keyword: String) {
        // google map search
    }

    override fun onStart() {
        super.onStart()

        setup()
        locationInfoZoom()
        locationInfoEdit()
        locationSearch()
        btnOverlay()
    }

    override fun onStop() {
        super.onStop()
        utilityProvider.setSpeed(speed)
    }

    override fun onResume() {
        super.onResume()

        if(locationChecker.isLocationEnabled()) {
            if(!locationChecker.checkLocationPermission())
                locationChecker.requestLocationPermission(this@MainActivity)
        } else {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            this@MainActivity.startActivity(settingsIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        utilityProvider.setSpeed(speed)
        mBinding = null
    }
}