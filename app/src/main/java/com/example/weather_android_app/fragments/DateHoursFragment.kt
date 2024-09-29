package com.example.weather_android_app.fragments

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_android_app.adapters.WeatherAdapter
import com.example.weather_android_app.databinding.FragmentDateHoursBinding
import com.example.weather_android_app.models.MainViewModel
import com.example.weather_android_app.models.WeatherModel
import org.json.JSONArray
import org.json.JSONObject

class DateHoursFragment : Fragment() {
    private lateinit var binding: FragmentDateHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDateHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        fillHoursPage()
    }

    private fun fillHoursPage(){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getHoursList(it))
        }
    }

    private fun getHoursList(wItem: WeatherModel): List<WeatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val item = WeatherModel(
                city = "",
                time = hour.getString("time"),
                condition = hour.getJSONObject("condition").getString("text"),
                currentTemp = hour.getString("temp_c") + "°C",
                maxTemp = "",
                minTemp = "",
                imageUrl = hour.getJSONObject("condition").getString("icon"),
                hours = ""
            )
            list.add(item)
        }
        return list
    }

    private fun initRcView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = DateHoursFragment()
    }
}