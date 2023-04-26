package com.iate_is_b_19z.laboratory_3.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.iate_is_b_19z.laboratory_3.API_KEY
import com.iate_is_b_19z.laboratory_3.AboutActivity
import com.iate_is_b_19z.laboratory_3.MainViewModel
import com.iate_is_b_19z.laboratory_3.checkPermissions
import com.iate_is_b_19z.laboratory_3.databinding.FragmentMainBinding
import com.iate_is_b_19z.laboratory_3.dayItem
import org.json.JSONObject

class MainFragment : Fragment() {
    private lateinit var pLauncher : ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private lateinit var fLocationClient: FusedLocationProviderClient

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){
        super.onViewCreated(view, savedInstanceState)

        aboutBtn.setOnClickListener {
            val intent = Intent(AboutActivity(), AboutActivity::class.java)
            startActivity(intent)
        }

        checkPermission()
        //getLocation()
    }

    /*private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
    }*/

    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, ct.token).addOnCompleteListener {
            //requestWeatherData("${it.result.latitude},${it.result.longitude}")
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" + city +
                "&days=3" +
                "&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                result -> parseWeatherData(result)
            },
            {
                error -> Toast.makeText(activity, "@strings/error_massage $error", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        parseCurrentData(mainObject)
        //val list = parseDays()
    }

    private fun parseCurrentData(mainObject: JSONObject) = with(binding) {
        val item = dayItem(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            "",
            mainObject.getJSONObject("current").getString("temp_c"),
            "0",
            "0",
            "0:00"
        )

        /*tvCityName.text = item.city
        tvDate.text = item.timestamp
        tvCondition.text = item.condition
        tvCurrentTemp.text = item.currentTemp
        tvMaxMin.text = item.maxTemp + "/" + item.minTemp*/
    }

    private fun checkPermission() {
        if(!checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(!checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }


    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Разрешение $it", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}