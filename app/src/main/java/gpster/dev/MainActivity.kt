package gpster.dev

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import gpster.dev.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding ?= null
    private val binding : ActivityMainBinding get() = requireNotNull(mBinding)

    private lateinit var locationChecker : MyLocationChecker
    private lateinit var utilityProvider : UtilityProvider
    private lateinit var fusedLocationProvider : FusedLocationProviderClient

    private var isRunning : Boolean = App.isRunning.value == true
    private var isRunningOverlay : Boolean = App.isRunningOverlay.value == true
    private var isExpanded : Boolean = true
    private var isEditOpen : Boolean = false

    private var lat : Double = App.location.value!!.lat
    private var lon : Double = App.location.value!!.lon
    private var alt : Double = App.location.value!!.alt
    private var speed : Double = App.speed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!isFinishing) {
            if(requestCode == 0 && grantResults.isNotEmpty()) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    requestLocation()
                else
                    finish()
            }
            if(requestCode == 1 && grantResults.isNotEmpty()) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    createNotificationChannel()
                else
                    finish()
            }
        }
    }

    private fun setup() {
        locationChecker = MyLocationChecker(this@MainActivity)
        utilityProvider = UtilityProvider(this@MainActivity)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        speed = utilityProvider.getSpeed()

        binding.root.setOnClickListener() {
            utilityProvider.hideKeyboard(this@MainActivity)
        }
    }

    private fun setupEt() {
        utilityProvider.apply {
            with(binding) {
                focusClear(listOf(
                    etLat, etLoc0, etLoc1, etLon, etAlt, etSpeed
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
                    ivZoom.visibility = View.GONE
                    ivEdit.setImageResource(R.drawable.ic_check)
                }

                isEditOpen = true
            } else {
                utilityProvider.hideKeyboard(this@MainActivity)

                lat = utilityProvider.parseToDouble(binding.etLat.text.toString(), -90.0, 90.0, lat, "%.4f")
                speed = utilityProvider.parseToDouble(binding.etSpeed.text.toString(), 0.0, 300.0, speed, "%.1f")

                if(!isExpanded) {
                    lon = utilityProvider.parseToDouble(binding.etLoc0.text.toString(), -180.0, 180.0, lon, "%.4f")
                    alt = utilityProvider.parseToDouble(binding.etLoc1.text.toString(), 0.0, 9999.9, alt, "%.1f")

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
                    lon = utilityProvider.parseToDouble(binding.etLon.text.toString(), -180.0, 180.0, lon, "%.4f")
                    alt = utilityProvider.parseToDouble(binding.etAlt.text.toString(), 0.0, 9999.9, alt, "%.1f")

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
                    ivZoom.visibility = View.VISIBLE
                    ivEdit.setImageResource(R.drawable.ic_edit)
                }

                isEditOpen = false

                locationInfo()
            }
        }
    }

    private fun locationSearch() {
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
            if(!app.isRunningOverlay) {
                if(!Settings.canDrawOverlays(this@MainActivity)) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    this@MainActivity.startActivity(settingsIntent)
                } else {
                    // overlay start
                    binding.btnOverlay.apply {
                        setText("종료")
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
                        setBackgroundResource(R.drawable.bg_outlined_box)
                    }

                    app.isRunningOverlay = true
                }
            } else {
                // overlay end
                binding.btnOverlay.apply {
                    setText("시작")
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    setBackgroundResource(R.drawable.bg_fill_box)
                }

                app.isRunningOverlay = false
            }
        }
    }

    private fun checkDefault() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(Settings.Secure.getInt(this@MainActivity.contentResolver, Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED) == 1) {
                if(locationChecker.isLocationEnabled()) {
                    if(!locationChecker.checkLocationPermission())
                        locationChecker.requestLocationPermission(this@MainActivity)
                    else if(!isRunning) {
                        createNotificationChannel()
                        requestLocation()
                    }
                } else {
                    val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    this@MainActivity.startActivity(settingsIntent)
                }
            } else {
                utilityProvider.toast("디버그 모드가 꺼져 있습니다")
                finish()
            }
        } else {
            utilityProvider.toast("해당 버전은 지원하지 않는 버전입니다")
            finish()
        }
    }

    private fun requestLocation() {
        if(locationChecker.checkLocationPermission()) {
            val serviceIntent = Intent(this@MainActivity, VirtualLocationService::class.java)
            fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    lat = "%.4f".format(it.latitude).toDouble()
                    lon = "%.4f".format(it.longitude).toDouble()
                    alt = "%.1f".format(it.altitude).toDouble()

                    startService(serviceIntent)
                } ?: run {
                    utilityProvider.toast("현재 위치를 가져올 수 없습니다")
                    startService(serviceIntent)
                }
            }
        }
    }

    private fun searchLocation(keyword: String) {
        // google map search
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            this@MainActivity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else {
            val nm = this@MainActivity.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                "gpster",
                "gpster",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            nm.createNotificationChannel(channel)
        }
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
        checkDefault()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null

        if(isRunning) {
            val serviceIntent = Intent(this@MainActivity, VirtualLocationService::class.java)
            stopService(serviceIntent)
        }
    }
}