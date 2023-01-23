package com.artyom.gpstracker_hdbh.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.artyom.gpstracker_hdbh.MainApp
import com.artyom.gpstracker_hdbh.MainViewModel
import com.artyom.gpstracker_hdbh.databinding.FragmentMainBinding
import com.artyom.gpstracker_hdbh.databinding.TracksBinding
import com.artyom.gpstracker_hdbh.databinding.ViewTrackBinding
import com.artyom.gpstracker_hdbh.db.TrackAdapter
import com.artyom.gpstracker_hdbh.db.TrackItem
import com.artyom.gpstracker_hdbh.utils.openFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 * Фрагмент в котором отображается список записанных в БД маршрутов
 */
class TracksFragment : Fragment(), TrackAdapter.Listener {
    private lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter

    /*MainViewModel*/
    private val model: MainViewModel by activityViewModels{

        /*В данном случае контекстом выступает класс MainApp : Application в котором инициализируется
        * экземпляр базы данных. Его и передаём в MainViewModel*/
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = TracksBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        getTracks()
    }

    private fun getTracks(){
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)

            /*Когда список маршрутов пуст показать "Пока пусто" */
            binding.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initRecyclerView() = with(binding){

        adapter = TrackAdapter(this@TracksFragment)

        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }

    /*Метод удаляет маршруты по нажатию*/
    override fun onClick(track: TrackItem, type: TrackAdapter.ClickType) {
        Log.d("MyLog", "TrackFragment кнопка удалить работает: Удалить маршрут с id = ${track.id}")
        Log.d("MyLog", "TrackFragment тип нажатия = $type")

        when(type){
            TrackAdapter.ClickType.DELETE -> model.deleteTrack(track)
            TrackAdapter.ClickType.OPEN -> openFragment(ViewTrackFragment.newInstance())
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = TracksFragment()
    }
}