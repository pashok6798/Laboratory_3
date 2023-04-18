package com.iate_is_b_19z.laboratory_3

import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

val API_KEY = "db33e9d486294afba84185656231504"

fun Fragment.checkPermissions(p: String): Boolean {
    return ContextCompat.checkSelfPermission(activity
      as AppCompatActivity, p) == PackageManager.PERMISSION_GRANTED
}

/*private fun getCity(latitude : Double, longitude : Double) : String {
    val geocoder = Geocoder(activity as AppCompatActivity, Locale.getDefault())
    val address = geocoder.getFromLocation(latitude, longitude, 1)

    return address!![0].locality
}*/