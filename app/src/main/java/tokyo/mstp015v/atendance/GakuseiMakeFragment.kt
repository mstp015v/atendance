package tokyo.mstp015v.atendance

import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import tokyo.mstp015v.atendance.databinding.FragmentGakuseiMakeBinding


class GakuseiMakeFragment : Fragment() {
    class GakuseiMakeFragmentAdapter(val data : List<Gakusei> ) : RecyclerView.Adapter<GakuseiMakeFragmentAdapter.ViewHolder>(){
        class ViewHolder( view : View):RecyclerView.ViewHolder( view ){
            val textIdMake = view.findViewById<TextView>(R.id.textIdMake)
            val editGakuseiIdMake = view.findViewById<EditText>(R.id.editGakuseiIdMake)
            val editNoMake = view.findViewById<EditText>(R.id.editNoMake)
            val editGakuseiMeiMake = view.findViewById<EditText>(R.id.editGakuseiMeiMake)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflate = LayoutInflater.from( parent.context).inflate(R.layout.gakusei_make_item,parent,false)
            return ViewHolder( inflate )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.textIdMake.text = data[position].id.toString()
            holder.editGakuseiIdMake.setText(data[position].gakusei_id)
            holder.editNoMake.setText(data[position].no.toString())
            holder.editGakuseiMeiMake.setText(data[position].gakusei_mei)


        }

        override fun getItemCount(): Int {
            return data.size

        }


    }

    private var _binding : FragmentGakuseiMakeBinding? = null
    private val binding get() = _binding!!
    private val args : GakuseiMakeFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentGakuseiMakeBinding.inflate( inflater , container,false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gakuseiList = arrayListOf<Gakusei>()

        val adapter = GakuseiMakeFragmentAdapter(gakuseiList)
        binding.recyclerGakuseiMake.adapter = adapter

    }

}