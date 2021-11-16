package tokyo.mstp015v.atendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import tokyo.mstp015v.atendance.databinding.FragmentTimeTableBinding
import tokyo.mstp015v.atendance.databinding.FragmentTimeTableContentsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TimeTableContentsFragment : Fragment() {

    class TimeTableRealmAdapter(data: OrderedRealmCollection<Risyuu>) :
        RealmRecyclerViewAdapter<Risyuu, TimeTableRealmAdapter.ViewHolder>(data,true) {

        private var listener:((Int,String)->Unit)? = null

        fun setOnItemClickListener(listener:((Int,String)->Unit)){
            this.listener = listener
        }

        class ViewHolder( cell: View) : RecyclerView.ViewHolder( cell ){
            val textTime : TextView = cell.findViewById<TextView>(R.id.textTime)
            val textKamoku : TextView = cell.findViewById<TextView>(R.id.textKamoku)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.time_table_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val risyuu:Risyuu?=getItem(position)
            holder.textTime.text = risyuu!!.jikan.toString()
            holder.textKamoku.text = risyuu!!.kamoku_mei
            holder.itemView.setOnClickListener {
                listener?.invoke( risyuu!!.jikan,risyuu!!.kamoku_mei)
            }
        }

        override fun getItemId(position: Int): Long {
            return super.getItemId(position)
        }
    }


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var y : Int = 0
    private var m : Int = 0
    private var d : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            y = it.getInt("Y")
            m = it.getInt("M")
            d = it.getInt("D")
        }
    }
    private var _binding : FragmentTimeTableContentsBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTimeTableContentsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        val realmResult = realm.where<Risyuu>().equalTo("kurasu_mei",param2 ).equalTo("youbi",param1).findAll()

        realmResult.forEach{
            Log.d("youbi",it.youbi)
            Log.d("kamoku",it.kamoku_mei)
        }

        val adapter = TimeTableRealmAdapter(realmResult)

        adapter.setOnItemClickListener{ jikan,kamoku_mei ->
            Log.d("kamoku_mei" , kamoku_mei)
            val action = TimeTableFragmentDirections
                .actionTimeTableFragmentToInputFragment(y,m,d,jikan,kamoku_mei,param2!!)

            findNavController().navigate( action )

        }

        binding.timeTableRecyclerView.adapter = adapter
        binding.timeTableRecyclerView.layoutManager = LinearLayoutManager(this.context )

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TimeTableContentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String,param2:String,y:Int,m:Int,d:Int) =
            TimeTableContentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)   //曜日
                    putString(ARG_PARAM2,param2)    //クラス
                    putInt("Y",y)
                    putInt("M",m)
                    putInt("D",d)
                }
            }
    }
}