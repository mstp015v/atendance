package tokyo.mstp015v.atendance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.atendance.databinding.FragmentGakuseiMakeKurasuListBinding

class GakuseiMakeKurasuListFragment : Fragment() {
    class MyAdapter( var data : List<String>): RecyclerView.Adapter<MyAdapter.ViewHolder>(){

        var listener : ((String)->Unit)? = null

        fun setOnItemClickListener( listener : ((String)->Unit )){
            this.listener = listener
        }

        class ViewHolder( view : View ): RecyclerView.ViewHolder( view ){
            val textKurasu = view.findViewById<TextView>(R.id.textKurasumei)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.gakusei_make_kurasu_list_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textKurasu.text = data[position]

            holder.itemView.setOnClickListener {
                listener!!.invoke( data[position])
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private var _binding : FragmentGakuseiMakeKurasuListBinding?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGakuseiMakeKurasuListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val realm = Realm.getDefaultInstance()
        val realmResult = realm.where<KurasuPermission>().findAll()

        val list = arrayListOf<String>()
        realmResult.forEach{
            list.add( it.kurasu_mei )
        }

        val adapter = MyAdapter( list )
        adapter.setOnItemClickListener {
            val action = GakuseiMakeKurasuListFragmentDirections.actionGakuseiMakeKurasuListFragmentToGakuseiMakeFragment(it)
            findNavController().navigate( action )
        }
        binding.recyclerKurasuList.adapter = adapter

    }
}

