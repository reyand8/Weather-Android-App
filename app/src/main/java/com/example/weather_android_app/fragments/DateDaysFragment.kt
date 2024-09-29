package com.example.weather_android_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_android_app.adapters.WeatherAdapter
import com.example.weather_android_app.databinding.FragmentDateDaysBinding
import com.example.weather_android_app.models.MainViewModel
import com.example.weather_android_app.models.WeatherModel


class DateDaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var binding: FragmentDateDaysBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDateDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        fillDaysPage()
    }

    private fun fillDaysPage(){
        model.liveDataList.observe(viewLifecycleOwner){
            adapter.submitList(it.subList(1, it.size))
        }
    }

    private fun initRcView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(this@DateDaysFragment)
        rcView.adapter = adapter
    }

    override fun onClick(item: WeatherModel) {
        model.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = DateDaysFragment()
    }
}