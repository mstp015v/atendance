package tokyo.mstp015v.atendance

import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.atendance.databinding.FragmentGakuseiMakeBinding


class GakuseiMakeFragment : Fragment() {
    class GakuseiMakeFragmentAdapter(val data : List<Gakusei> ) : RecyclerView.Adapter<GakuseiMakeFragmentAdapter.ViewHolder>(){

        private var idListener : ((String)->Unit)? = null
        private var noListener :((Int)->Unit)?=null
        private var meiListener : ((String)->Unit)?=null

        fun setEditGakuseiMeiChangeListener( listener: ((String)->Unit) ){
            this.meiListener = listener
        }
        fun setEditGakuseiIdChangeListener( listener : ((String)->Unit)){
            this.idListener = listener
        }
        fun setEditNoChangeListener( listener : ((Int)->Unit)){
            this.noListener = listener
        }

        class ViewHolder( view : View):RecyclerView.ViewHolder( view ){
            val editGakuseiIdMake = view.findViewById<EditText>(R.id.editGakuseiIdMake)
            val editNoMake = view.findViewById<EditText>(R.id.editNoMake)
            val editGakuseiMeiMake = view.findViewById<EditText>(R.id.editGakuseiMeiMake)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflate = LayoutInflater.from( parent.context).inflate(R.layout.gakusei_make_item,parent,false)
            return ViewHolder( inflate )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.editGakuseiIdMake.setText(data[position].gakusei_id)
            holder.editNoMake.setText(data[position].no.toString())
            holder.editGakuseiMeiMake.setText(data[position].gakusei_mei)

            holder.editGakuseiMeiMake.doAfterTextChanged {
                meiListener!!.invoke( data[position].gakusei_mei )
            }
            holder.editGakuseiIdMake.doAfterTextChanged {
                idListener!!.invoke( data[position].gakusei_id)
            }
            holder.editNoMake.doAfterTextChanged {
                noListener!!.invoke( data[position].no )
            }

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

        //クラス名を表示する
        binding.textKurasuMeiGakuseiMake.text = args.kurasuMei

        //RecyclerViewに表示する学生リストを作成する
        val gakuseiList = arrayListOf<Gakusei>()
        Realm.getDefaultInstance().use{ realm->
            val ret = realm.where<Gakusei>().equalTo("kurasu_mei",args.kurasuMei).sort("no").findAll()
            var id :Long = (realm.where<Gakusei>().max("id") ?: 0L).toLong()
            id = id + 1L
            if( ret.size > 0 ){
                //学生がrealmに存在する
                ret.forEach{
                    val gakusei = Gakusei(it.id,it.gakusei_id,it.no,it.gakusei_mei,it.kurasu_mei)
                    gakuseiList.add( gakusei )
                }
            }else{
                //学生がrealmに存在しない
                val gakusei = Gakusei(id,"",0,"",args.kurasuMei)
                gakuseiList.add( gakusei )

                realm.executeTransaction{db->
                    val g = db.createObject<Gakusei>( gakusei.id )
                    g.kurasu_mei = gakusei.kurasu_mei
                }

            }
        }

        val adapter = GakuseiMakeFragmentAdapter(gakuseiList)
        binding.recyclerGakuseiMake.adapter = adapter
        binding.recyclerGakuseiMake.layoutManager = LinearLayoutManager( context )

        //追加イベント
        binding.buttonGakuseiAddMake.setOnClickListener {

            //realmにも追加しておく
            Realm.getDefaultInstance().use{ realm ->
                val max = (realm.where<Gakusei>().max("id") ?: 0L).toLong()
                val gakusei = Gakusei( max+1,"",0,"",args.kurasuMei )
                gakuseiList.add( gakusei )
                realm.executeTransaction{ db->
                    val g = db.createObject<Gakusei>( gakusei.id )
                    g.kurasu_mei = gakusei.kurasu_mei
                }
            }

            adapter.notifyDataSetChanged()
        }

        //更新イベント
        binding.buttonUpMake.setOnClickListener {
            Realm.getDefaultInstance().use{ realm->
                gakuseiList.forEach{ gakusei->
                    realm.executeTransaction { db ->
                        val ret = db.where<Gakusei>().equalTo("id", gakusei.id).findFirst()

                        if (ret == null) {
                            //いないので追加
                        } else {
                            //いるので更新
                            ret.gakusei_mei = gakusei.gakusei_mei
                            ret.gakusei_id = gakusei.gakusei_id
                            ret.no = gakusei.no
                            ret.kurasu_mei = gakusei.kurasu_mei
                        }
                    }
                }

            }
        }
    }

}
