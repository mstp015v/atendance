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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.atendance.databinding.FragmentInputListBinding

class InputListFragment : Fragment() {
    class InputListAdapter( val dataSet:ArrayList<String>): RecyclerView.Adapter<InputListAdapter.ViewHolder>() {

        private var listener: ((String?) -> Unit)? = null

        fun setOnItemClickListener( listener:(String?)->Unit){
            this.listener = listener
        }

        class ViewHolder(view: View): RecyclerView.ViewHolder(view){
            var textKurasu1 : TextView
            init{
                textKurasu1 = view.findViewById(R.id.textKurasu1)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from( parent.context ).inflate(R.layout.input_list_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textKurasu1.text = dataSet[position]
            holder.textKurasu1.setOnClickListener{
                listener?.invoke( dataSet[position] )
            }


        }
        override fun getItemCount() = dataSet.size
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding : FragmentInputListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInputListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        //val risyuuRealm = realm.where<Risyuu>().distinct("kurasu_mei").findAll()
        val list = ArrayList<String>()
        //risyuuRealm.forEach {
        //    list.add( it.kurasu_mei )
        //    Log.d("inputlistfragment",it.kurasu_mei)
        //}
        val account = GoogleSignIn.getLastSignedInAccount(this.context)
        val kurasupermission = realm.where<KurasuPermission>().equalTo("tantou_account",account.email ).distinct("kurasu_mei").findAll()
        kurasupermission.forEach {
            list.add( it.kurasu_mei )
        }
        val adapter = InputListAdapter(list )
        binding.recyclerViewInputList.adapter = adapter
        binding.recyclerViewInputList.layoutManager = LinearLayoutManager(context)
        adapter.setOnItemClickListener {
            val action = InputListFragmentDirections.actionInputListFragmentToTimeTableFragment(it!!)
            findNavController().navigate( action )
        }
        realm.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}