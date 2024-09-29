package com.example.weather_android_app.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.example.weather_android_app.DialogManager
import com.example.weather_android_app.R
import com.example.weather_android_app.adapters.FragmentAdapter
import com.example.weather_android_app.databinding.FragmentMainBinding
import com.example.weather_android_app.extensions.isPermissionGranted
import com.example.weather_android_app.models.MainViewModel
import com.example.weather_android_app.models.WeatherModel

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

const val API_KEY = "YOUR TOKEN"

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val model: MainViewModel by activityViewModels()

    private val fragmentList = listOf(
        DateHoursFragment.newInstance(),
        DateDaysFragment.newInstance(),
    )

    private val timeList = listOf(
        "Hours",
        "Days",
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =  with(binding) {
        super.onViewCreated(view, savedInstanceState)
        progressBar.visibility = View.VISIBLE
        cardView.visibility = View.GONE
        tabLayout.visibility = View.GONE
        vp.visibility = View.GONE
        constraintLayout.visibility = View.GONE
        checkPermission()
        init()
        updateCurrentCard()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding){
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = FragmentAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
                tab, pos -> tab.text = timeList[pos]
        }.attach()
        ibSync.setOnClickListener{
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        ibSearch.setOnClickListener{
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){
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
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun updateCurrentCard() = with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            val maxMinTemp = "${it.minTemp}℃ / ${it.maxTemp}℃"
            val maxMinTempRound = "${it.minTemp.toDouble().roundToInt()}°C/${it.maxTemp.toDouble()
                .roundToInt()}°C"
            val currentTemp = "${it.currentTemp}℃"
            itemDate.text = it.time
            itemCity.text = it.city
            itemCondition.text = it.condition
            itemCurrentTemp.text = if(it.currentTemp.isEmpty()) maxMinTempRound else currentTemp
            itemMaxMin.text = if(it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:" + it.imageUrl).into(itemImage)
        }
    }


    private fun timeLastUpd(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formatted = LocalDateTime.parse(date, formatter)
        return if (formatted.minute.toString() == "0") {
            "Last updated: ${formatted.hour}:${formatted.minute}0\u00A0"
        } else {
            "Last updated: ${formatted.hour}:${formatted.minute}\u00A0"
        }
    }

    private fun permissionListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission(){
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(location: String) = with(binding){
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                location +
                "&days=" +
                "6" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                    result ->
                progressBar.visibility = View.GONE
                cardView.visibility = View.VISIBLE
                tabLayout.visibility = View.VISIBLE
                vp.visibility = View.VISIBLE
                constraintLayout.visibility = View.VISIBLE
                parseWeatherData(result)
            },
            {
                    error -> Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }


    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel){
        val timeHoursMin = timeLastUpd(mainObject.getJSONObject("current")
            .getString("last_updated"))
        val conditionText = mainObject.getJSONObject("current")
            .getJSONObject("condition").getString("text")
        val item = WeatherModel(
            city = mainObject.getJSONObject("location").getString("name"),
            time = timeHoursMin,
            condition = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
            maxTemp = weatherItem.maxTemp,
            minTemp = weatherItem.minTemp,
            imageUrl = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("icon"),
            hours = weatherItem.hours
        )
        editWeatherBg(conditionText)
        model.liveDataCurrent.value = item
    }

    private fun editWeatherBg(conditionText: String) = with(binding){
        if (conditionText == "Clear") {
            imageViewBg.setImageResource(R.drawable.bg_night)
            ibSync.setImageResource(R.drawable.baseline_sync_white_26)
            ibSearch.setImageResource(R.drawable.baseline_search_white_26)
        }
        if (conditionText == "Sunny") {
            imageViewBg.setImageResource(R.drawable.bg_sunny)
        }
        if (conditionText == "Partly cloudy" || conditionText == "Patchy rain nearby") {
            imageViewBg.setImageResource(R.drawable.bg_cloudy)
        }
        if (conditionText == "Cloudy"
            || conditionText == "Overcast"
            || conditionText == "Light drizzle"
            || conditionText == "Light rain"
            || conditionText == "Light rain shower"
            || conditionText == "Moderate rain"
            || conditionText == "Heavy rain"
            || conditionText == "Moderate or heavy rain shower") {
            imageViewBg.setImageResource(R.drawable.bg_rain)
            ibSync.setImageResource(R.drawable.baseline_sync_white_26)
            ibSearch.setImageResource(R.drawable.baseline_search_white_26)
        }
        if (conditionText == "Fog" || conditionText == "Mist") {
            binding.imageViewBg.setImageResource(R.drawable.bg_fog)
        }
        else {
            imageViewBg.setImageResource(R.drawable.bg_sunny)
        }
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                city = name,
                time = day.getString("date"),
                condition = day.getJSONObject("day")
                    .getJSONObject("condition").getString("text"),
                currentTemp = "",
                maxTemp = day.getJSONObject("day").getString("maxtemp_c"),
                minTemp = day.getJSONObject("day").getString("mintemp_c"),
                imageUrl = day.getJSONObject("day")
                    .getJSONObject("condition").getString("icon"),
                hours = day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}