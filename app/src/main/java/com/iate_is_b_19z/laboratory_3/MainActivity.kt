package com.iate_is_b_19z.laboratory_3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import org.json.JSONObject
import java.util.Locale

/*fun getWeather() {

}*/

val API_KEY = "db33e9d486294afba84185656231504"

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private fun checkPermissions(): Boolean {
        return (checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun getCity(latitude : Double, longitude : Double) : String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(latitude, longitude, 1)

        return address!![0].locality
    }

    private val PERMISSION_ID = 42

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location? = locationResult.lastLocation

            findViewById<TextView>(R.id.shortTV).text = mLastLocation?.latitude.toString()
            findViewById<TextView>(R.id.longTV).text = mLastLocation?.longitude.toString()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        //findViewById<TextView>(R.id.shortTV).text = location.latitude.toString()
                        //findViewById<TextView>(R.id.longTV).text = location.longitude.toString()

                        var lat = location.latitude
                        var lon = location.longitude

                        //var lang = "en"

                        //if (Locale.getDefault().language == "ru") lang = "ru"

                        val cityName = getCity(lat, lon)

                        if(cityName != null) {
                            val url =
                                "http://api.weatherapi.com/v1/current.json?key=$API_KEY&q=$cityName&aqi=no"
                            val queue = Volley.newRequestQueue(this)
                            val stringRequest = StringRequest(Request.Method.GET,
                                url,
                                { response ->
                                    val obj = JSONObject(response)
                                    val curr = obj.getJSONObject("current")
                                    val loc = obj.getJSONObject("location")

                                    findViewById<TextView>(R.id.shortTV).text = "Температура (Цельсий): " + curr.getString("temp_c")
                                    findViewById<TextView>(R.id.longTV).text = loc.getString("name")
                                    //findViewById<TextView>(R.id.helloWorldTV).text = "Город: " + loc.getString("name") + ", температура (Цельсий): " + curr.getString("temp_c")
                                },
                                {
                                    Toast.makeText(this, "Volley error: $it", Toast.LENGTH_LONG)
                                    findViewById<TextView>(R.id.helloWorldTV).text = "Volley error: $it"
                                })

                            queue.add(stringRequest)
                            queue.start()

                            //findViewById<TextView>(R.id.helloWorldTV).text = cityName
                        }
                        else {
                            Toast.makeText(this, "Ошибка. Пустой адрес", Toast.LENGTH_SHORT)
                        }
                    }
                }
            } else {
                Toast.makeText(this, this.getString(R.string.enableGeolocation), Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
    }
}