package gpster.dev

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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

    private lateinit var handler : Handler
    private lateinit var locationChecker : MyLocationChecker
    private lateinit var utilityProvider : UtilityProvider
    private lateinit var fusedLocationProvider : FusedLocationProviderClient

    private var isExpanded : Boolean = true
    private var isEditOpen : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        utilityProvider.setSpeed(App.speed)
    }

    override fun onResume() {
        super.onResume()
        checkDefault()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null

        if(App.isRunning) {
            val serviceIntent = Intent(this@MainActivity, VirtualLocationService::class.java)
            stopService(serviceIntent)
        }
        if(App.isRunningOverlay) {
            val serviceIntent = Intent(this@MainActivity, OverlayService::class.java)
            stopService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!isFinishing) {
            if(requestCode == 0 && grantResults.isNotEmpty()) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
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

    private val updateRunnable = object : Runnable {
        override fun run() {
            locationInfo()
            handler.postDelayed(this, 100)
        }
    }

    private fun checkDefault() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(Settings.Secure.getInt(this@MainActivity.contentResolver, Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED) == 1) {
                if(locationChecker.isLocationEnabled()) {
                    if(!locationChecker.checkLocationPermission())
                        locationChecker.requestLocationPermission(this@MainActivity)
                    else if(!App.isRunning) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            this@MainActivity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else {
            val nm = this@MainActivity.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                App.CHANNEL_ID,
                "gpster",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            nm.createNotificationChannel(channel)
        }
    }

    private fun requestLocation() {
        if(locationChecker.checkLocationPermission()) {
            val serviceIntent = Intent(this@MainActivity, VirtualLocationService::class.java)
            fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    App.lat = "%.4f".format(it.latitude).toDouble()
                    App.lon = "%.4f".format(it.longitude).toDouble()
                    App.alt = "%.1f".format(it.altitude).toDouble()

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

    private fun setup() {
        handler = Handler(Looper.getMainLooper())
        locationChecker = MyLocationChecker(this@MainActivity)
        utilityProvider = UtilityProvider(this@MainActivity)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        App.speed = utilityProvider.getSpeed()

        handler.postDelayed(updateRunnable, 100)
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
                tvLat.setText(App.lat.toString())
                tvLon.setText(App.lon.toString())
                tvAlt.setText(App.alt.toString())
            }
        } else {
            binding.tvLat.setText("${App.lat}, ${App.lon}, ${App.alt}")
        }
        binding.tvSpeed.setText("${App.speed} km/h")
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

                App.lat = utilityProvider.parseToDouble(binding.etLat.text.toString(), -90.0, 90.0, App.lat, "%.4f")
                App.speed = utilityProvider.parseToDouble(binding.etSpeed.text.toString(), 0.0, 300.0, App.speed, "%.1f")

                if(!isExpanded) {
                    App.lon = utilityProvider.parseToDouble(binding.etLoc0.text.toString(), -180.0, 180.0, App.lon, "%.4f")
                    App.alt = utilityProvider.parseToDouble(binding.etLoc1.text.toString(), 0.0, 9999.9, App.alt, "%.1f")

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
                    App.lon = utilityProvider.parseToDouble(binding.etLon.text.toString(), -180.0, 180.0, App.lon, "%.4f")
                    App.alt = utilityProvider.parseToDouble(binding.etAlt.text.toString(), 0.0, 9999.9, App.alt, "%.1f")

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
        val serviceIntent = Intent(this@MainActivity, OverlayService::class.java)
        binding.btnOverlay.setOnClickListener() {
            if(!App.isRunningOverlay) {
                if(!Settings.canDrawOverlays(this@MainActivity)) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    this@MainActivity.startActivity(settingsIntent)
                } else {
                    startService(serviceIntent)
                    binding.btnOverlay.apply {
                        setText("종료")
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
                        setBackgroundResource(R.drawable.bg_outlined_box)
                    }
                }
            } else {
                stopService(serviceIntent)
                binding.btnOverlay.apply {
                    setText("시작")
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    setBackgroundResource(R.drawable.bg_fill_box)
                }
            }
        }
    }
}