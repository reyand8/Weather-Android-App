package com.example.weather_android_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather_android_app.R
import com.example.weather_android_app.databinding.ListItemBinding
import com.example.weather_android_app.models.WeatherModel
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt


class WeatherAdapter(val listener: Listener?)
    : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {
    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemTemp: WeatherModel? = null

        init {
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: WeatherModel) = with(binding){
            val tvDateItalic = "${item.time}\u00A0"
            itemTemp = item
            tvDate.text = tvDateItalic
            tvCondition.text = item.condition
            tvTemp.text = item.currentTemp.ifEmpty {
                "${item.minTemp.toDouble().roundToInt()}°C/${item.maxTemp.toDouble().roundToInt()}°C"
            }
            Picasso.get().load("https:" + item.imageUrl).into(im)
        }

    }

    class Comparator : DiffUtil.ItemCallback<WeatherModel>(){
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(item: WeatherModel)
    }
}