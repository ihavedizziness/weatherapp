package com.example.weatheraapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.weatheraapp.adapters.OpenWeatherMapAdapter
import com.example.weatheraapp.adapters.VisualCrossingTimelineAdapter
import com.example.weatheraapp.adapters.WeatherApiAdapter
import com.example.weatheraapp.model.OpenWeatherMapData
import com.example.weatheraapp.model.VisualCrossingTimelineData
import com.example.weatheraapp.model.WeatherApiData
import com.example.weatheraapp.model.WeatherData
import com.example.weatheraapp.services.OpenWeatherMapService
import com.example.weatheraapp.services.VisualCrossingTimelineService
import com.example.weatheraapp.services.WeatherApiService
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mProgressDialog: Dialog? = null

    private lateinit var tvMainDescription: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvHumidity: TextView

    private lateinit var openWeatherMapButton: Button
    private lateinit var weatherAPIButton: Button
    private lateinit var visualCrossingWeatherButton: Button

    private var currentApi: String = "OpenWeatherMapAPI"
    private var weatherData: WeatherData? = null

    private val openWeatherMapAdapter = OpenWeatherMapAdapter()
    private val weatherApiAdapter = WeatherApiAdapter()
    private val visualCrossingTimelineAdapter = VisualCrossingTimelineAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initView()

        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "ur location provider is turned off. pls turn it on :-)",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "u have denied location permission",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }

        openWeatherMapButton.setOnClickListener {
            currentApi = "OpenWeatherMapAPI"
        }
        weatherAPIButton.setOnClickListener {
            currentApi = "WeatherAPI"
        }
        visualCrossingWeatherButton.setOnClickListener {
            currentApi = "VisualCrossingWeatherAPI"
        }
    }

    private fun initView() {
        tvMainDescription = findViewById(R.id.tv_main_description)
        tvTemp = findViewById(R.id.tv_temp)
        tvHumidity = findViewById(R.id.tv_humidity)

        openWeatherMapButton = findViewById(R.id.btn_open_weather_map)
        weatherAPIButton = findViewById(R.id.btn_weather_api)
        visualCrossingWeatherButton = findViewById(R.id.btn_visual_crossing_weather)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        mFusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build(),
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            val latitude = mLastLocation?.latitude
            Log.i("cur latitude", "$latitude")

            val longitude = mLastLocation?.longitude
            Log.i("cur longitude", "$longitude")

            getLocationWeatherDetails(latitude!!, longitude!!)
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(this)) {
            Log.i("cur api", "$currentApi")
            when (currentApi) {
                "OpenWeatherMapAPI" -> {
                    val retrofitOpenWeatherMap = Retrofit.Builder()
                        .baseUrl(Constants.OPEN_WEATHER_MAP_API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val openWeatherMapService =
                        retrofitOpenWeatherMap.create(OpenWeatherMapService::class.java)

                    val listCall: Call<OpenWeatherMapData> = openWeatherMapService.getWeather(
                        latitude,
                        longitude,
                        Constants.METRIC_UNIT,
                        Constants.OPEN_WEATHER_MAP_API_KEY
                    )

//                    showCustomProgressDialog()

                    listCall.enqueue(object : Callback<OpenWeatherMapData> {
                        override fun onResponse(
                            call: Call<OpenWeatherMapData>,
                            response: Response<OpenWeatherMapData>
                        ) {
                            if (response.isSuccessful) {
//                                hideProgressDialog()

                                val apiData: OpenWeatherMapData = response.body()!!

                                Log.i("OpenWeatherMapAPI res", "$apiData")

                                weatherData = openWeatherMapAdapter.convertToStandardFormat(apiData)
                            } else {
                                val rc = response.code()
                                when (rc) {
                                    400 -> {
                                        Log.i("Error 400", "Bad Connection")
                                    }

                                    404 -> {
                                        Log.i("Error 404", "Not Found")
                                    }

                                    else -> {
                                        Log.i("Error", "Generic Error")
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<OpenWeatherMapData>, t: Throwable) {
                            Log.e("Errrror", t.message.toString())
//                            hideProgressDialog()
                        }

                    })

                    weatherData?.let { setupUI(it) }
                }
                "WeatherAPI" -> {
                    val retrofitWeatherAPi = Retrofit.Builder()
                        .baseUrl(Constants.WEATHER_API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val weatherApiService = retrofitWeatherAPi.create(WeatherApiService::class.java)

                    val listCall: Call<WeatherApiData> = weatherApiService.getWeather(
                        Constants.WEATHER_API_KEY, "$latitude,$longitude", "no"
                    )

//                    showCustomProgressDialog()

                    listCall.enqueue(object : Callback<WeatherApiData> {
                        override fun onResponse(
                            call: Call<WeatherApiData>,
                            response: Response<WeatherApiData>
                        ) {
                            if (response.isSuccessful) {
//                                hideProgressDialog()

                                val apiData: WeatherApiData = response.body()!!

                                Log.i("WeatherAPI res", "$apiData")

                                weatherData = weatherApiAdapter.convertToStandardFormat(apiData)
                            } else {
                                val rc = response.code()
                                when (rc) {
                                    400 -> {
                                        Log.i("Error 400", "Bad Connection")
                                    }

                                    404 -> {
                                        Log.i("Error 404", "Not Found")
                                    }

                                    else -> {
                                        Log.i("Error", "Generic Error")
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<WeatherApiData>, t: Throwable) {
                            Log.e("Errrror", t.message.toString())
//                            hideProgressDialog()
                        }

                    })

                    weatherData?.let { setupUI(it) }
                }
                "VisualCrossingWeatherAPI" -> {
                    val retrofitVisualCrossingTimeline = Retrofit.Builder()
                        .baseUrl(Constants.VISUAL_CROSSING_TIMELINE_WEATHER_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val visualCrossingTimelineService =
                        retrofitVisualCrossingTimeline.create(VisualCrossingTimelineService::class.java)

                    val listCall: Call<VisualCrossingTimelineData> = visualCrossingTimelineService.getWeather(
                        "$latitude,$longitude", Constants.VISUAL_CROSSING_TIMELINE_WEATHER_KEY
                    )

//                    showCustomProgressDialog()

                    listCall.enqueue(object : Callback<VisualCrossingTimelineData> {
                        override fun onResponse(
                            call: Call<VisualCrossingTimelineData>,
                            response: Response<VisualCrossingTimelineData>
                        ) {
                            if (response.isSuccessful) {
//                                hideProgressDialog()

                                val apiData: VisualCrossingTimelineData = response.body()!!

                                Log.i("VisualCrossingTimelineAPI res", "$apiData")

                                weatherData = visualCrossingTimelineAdapter.convertToStandardFormat(apiData)
                            } else {
                                val rc = response.code()
                                when (rc) {
                                    400 -> {
                                        Log.i("Error 400", "Bad Connection")
                                    }

                                    404 -> {
                                        Log.i("Error 404", "Not Found")
                                    }

                                    else -> {
                                        Log.i("Error", "Generic Error")
                                    }
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<VisualCrossingTimelineData>,
                            t: Throwable
                        ) {
                            Log.e("Errrror", t.message.toString())
//                            hideProgressDialog()
                        }

                    })

                    weatherData?.let { setupUI(it) }
                }
                else -> throw IllegalArgumentException("Unsopported API")
            }

        } else {
            Toast.makeText(
                this@MainActivity,
                "no internet connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("it looks like u have turned off permissions required for this feature. it can be enabled under app settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) {_, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(
                "CANCEL"
            ) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun setupUI(data: WeatherData) {
        tvMainDescription.text = data.weatherDescription
        tvTemp.text = "${data.temp}°C"
        tvHumidity.text = "${data.humidity}%"
    }
}