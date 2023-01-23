package com.artyom.gpstracker_hdbh.db

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.databinding.TrackItemBinding

class TrackAdapter(private val listener: Listener): ListAdapter<TrackItem, TrackAdapter.Holder>(TrackAdapter.Comparator()) {


    class Holder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view), View.OnClickListener{

        /*Биндинг уже надутой разметки*/
        private val binding = TrackItemBinding.bind(view)

        /*track - который нужно удалить*/
        private var trackTemp: TrackItem? = null

        init{
            /*Срабатывает на удаление*/
            binding.ibDelete.setOnClickListener(this)

            /*Тот же самый слушатель установлен и на запуск фрагмента ViewTrackFragment */
            binding.item.setOnClickListener(this)

        }


        override fun onClick(view: View?) {
            /*Если маршрут не построен, но кнопка нажата, то trackTemp = null
            * безопасно. как только будет маршрут*/
            Log.d("MyLog", "TrackAdapter.Holder::onClick()")

            /*Проверка что нужно сделать */
            val type =  when(view!!.id){
                R.id.ibDelete -> ClickType.DELETE
                R.id.item -> ClickType.OPEN
                else -> ClickType.OPEN

            }
            trackTemp?.let() { listener.onClick(it, type) }

        }



        fun bind(track: TrackItem) = with(binding){

            trackTemp = track

            val speed = "${track.speed} km/h"
            val time = "${track.time} m"
            val distance = "${track.distance} km"

            tvData.text = track.date
            tvSpeed.text = speed
            tvTime.text = time
            tvDistance.text = distance
        }

    }

    /*Компаратор сравнивает два объекта списка и определяет нужно ли перерисовывать его элементы*/
    class Comparator : DiffUtil.ItemCallback<TrackItem>(){

        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }
    }

    /*on-Create-View-Holder*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)

        return Holder(view, listener)
    }

    /*on-Bind-View-Holder*/
    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bind(getItem(position))
    }

    /*Интерфейс, который будет реализован в TrackFragment*/
    interface Listener{
        /*Метод удаляет маршрут по нажатию на кнопку удалить*/
        fun onClick(track: TrackItem, type: ClickType)
    }

    /*Агрегирует в себе тип нажатия.
    * Либо это кнопка, либо элмент списка*/
    enum class ClickType{
        DELETE,
        OPEN
    }
}