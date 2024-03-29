package tokyo.mstp015v.atendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.coroutines.*
import tokyo.mstp015v.atendance.databinding.FragmentKurasuMakeBinding

class KurasuMakeFragment : Fragment() {
    class MyAdapter( var data : List<KurasuPermission> ) : RecyclerView.Adapter<MyAdapter.ViewHolder>(){
        private var listener1 : ((position:Int,value:String)->Unit)? = null
        private var listener2 : ((position:Int,value:String)->Unit)? = null
        private var listener3 : ((position:Int)->Unit)? = null

        fun setKurasuMeiAfterListener( listener : ((position:Int,value:String)->Unit)){
            this.listener1 = listener
        }

        fun setAccountAfterListener( listener : ((position:Int,value:String)->Unit)){
            this.listener2 = listener
        }

        fun setDeleteListener( listener : ((position:Int)->Unit)){
            this.listener3 = listener
        }
        class ViewHolder(view : View):RecyclerView.ViewHolder( view ){
            val textId = view.findViewById<TextView>(R.id.textIdKurasuMake )
            val editKurasuMei = view.findViewById<EditText>(R.id.editKurasuMeiKurasuMake)
            val editAccount = view.findViewById<EditText>(R.id.editAccountKurasuMake)
            val imageDelete = view.findViewById<ImageView>(R.id.imageDeleteKurasuMake)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.kurasu_make_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textId.text = data[position].id.toString()
            holder.editKurasuMei.setText(data[position].kurasu_mei)
            holder.editAccount.setText( data[position].tantou_account)

            holder.editKurasuMei.doAfterTextChanged {
                listener1?.invoke( position , holder.editKurasuMei.text.toString() )
            }

            holder.editAccount.doAfterTextChanged {
                listener2?.invoke( position , holder.editAccount.text.toString() )
            }

            holder.imageDelete.setOnClickListener {
                listener3?.invoke( position )

            }
            Log.d("onBind",data[position].id.toString())
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private var _binding : FragmentKurasuMakeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentKurasuMakeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //RecyclerViewに表示するための設定
        val list = arrayListOf<KurasuPermission>()
        val adapter = MyAdapter( list )
        Realm.getDefaultInstance().use{
            it.executeTransaction{
                var realmRet : RealmResults<KurasuPermission>? = null
                realmRet = it.where<KurasuPermission>().findAll()

                Log.d("realmRetSize",realmRet!!.size.toString())
                if( realmRet!!.size > 0 ) {
                    realmRet!!.forEach {
                        val item = KurasuPermission(
                            it.id, it.kurasu_mei, it.tantou_account
                        )

                        list.add(item)
                    }
                }else{
                    list.add( KurasuPermission(1,"",""))
                }
            }
        }

        binding.recyclerViewKurasuMake.adapter = adapter
        binding.recyclerViewKurasuMake.layoutManager = LinearLayoutManager(context )

        //adapterのイベント
        adapter.setKurasuMeiAfterListener { position, value ->
            list[position].kurasu_mei = value
            Log.d("event", value )
        }

        adapter.setAccountAfterListener { position, value ->
            list[position].tantou_account = value
            Log.d("event", value )
        }

        adapter.setDeleteListener {
            Realm.getDefaultInstance().use{realm->
                val kurasu = list.removeAt( it )
                val ret = realm.where<KurasuPermission>().equalTo("id",kurasu.id).findAll()
                Log.d("delete", kurasu.id.toString() )
                realm.executeTransaction {
                    ret.deleteAllFromRealm()
                }
            }
            adapter.notifyDataSetChanged()
        }

        //追加ボタンのイベント
        binding.buttonAddKurasuMake.setOnClickListener {
            var max = 0L

            list.forEach {
                if( max < it.id ){
                    max = it.id
                }
            }

            val item = KurasuPermission(max + 1, "","")
            list.add( item )

            Realm.getDefaultInstance().use{ realm->
                realm.executeTransaction{ db->
                    db.createObject<KurasuPermission>( item.id )
                }
            }

            adapter.notifyDataSetChanged()

            Log.d( "click",list.size.toString() )
        }

        //realmに登録する
        binding.buttonUpKurasuMake.setOnClickListener {
            Realm.getDefaultInstance().use{ realm->
                realm.executeTransactionAsync{ db->
                    list.forEach{

                        val realmRet = db.where<KurasuPermission>().equalTo("id",it.id).findFirst()

                        if( realmRet == null ){
                            //存在しないので追加
                            val maxId = ( db.where<KurasuPermission>().max("id") ?: 0L).toLong()
                            Log.d("transaction" , "add" )
                            val item = db.createObject<KurasuPermission>(maxId+1)
                            item.kurasu_mei = it.kurasu_mei
                            item.tantou_account = it.tantou_account
                        }else{
                            //存在するので更新
                            Log.d("transaction" , realmRet.id.toString() )
                            realmRet?.kurasu_mei = it.kurasu_mei
                            realmRet?.tantou_account = it.tantou_account
                        }

                    }
                }
            }
            Snackbar.make(binding.root,"追加更新完了",Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}