package tokyo.mstp015v.atendance

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.atendance.databinding.FragmentInputBinding

class InputFragment : Fragment() {


    class InputFragmentRecyclerViewAdapter(var data : List<Syusseki> )
        : RecyclerView.Adapter<InputFragmentRecyclerViewAdapter.ViewHolder>(){

        private var listener : ((syusseki_code:Int,position:Int)->Unit)? = null

        fun setItemOnClickListener( listener: ((syusseki_code:Int,position:Int)->Unit)? ){
            this.listener = listener
        }

        class ViewHolder(view : View ) : RecyclerView.ViewHolder( view ){
            val textNo = view.findViewById<TextView>(R.id.textNo)
            val textNamae = view.findViewById<TextView>(R.id.textNamae )
            val radiogroup = view.findViewById<RadioGroup>(R.id.radioGroup)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.syusseki_input_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val syusseki:Syusseki? = data[position]
            holder.textNo.text = syusseki!!.no.toString()
            holder.textNamae.text = syusseki!!.gakusei_mei
            Log.d("Input onBind","${syusseki!!.no},${syusseki.gakusei_mei},${syusseki.kamoku_mei}")

            when( syusseki!!.syusseki_code ){
                0->{
                    holder.radiogroup.check(R.id.radio0)
                }
                1->{
                    holder.radiogroup.check(R.id.radio1)
                }
                2->{
                    holder.radiogroup.check(R.id.radio2)
                }
                3->{
                    holder.radiogroup.check(R.id.radio3)
                }
            }

            //イベントリスナー
            holder.radiogroup.setOnCheckedChangeListener { group, checkedId ->

                when(checkedId){
                    R.id.radio0->{
                        listener!!.invoke(0,position)
                    }
                    R.id.radio1->{
                        listener!!.invoke(1,position)
                    }
                    R.id.radio2->{
                        listener!!.invoke(2,position)
                    }
                    R.id.radio3->{
                        listener!!.invoke(3,position)
                    }
                }
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }


    }

    private val syussekiList : ArrayList<Syusseki> = arrayListOf()
    private val args : InputFragmentArgs by navArgs()
    private var _binding : FragmentInputBinding? = null
    private val binding get() = _binding!!
    private var existsSyussekiRealm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInputBinding.inflate( inflater , container , false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInputYmd.text = "${args.nen}年${args.tsuki+1}月${args.hi}日${args.jikan}時間目"
        binding.editKamokuMei.hint = args.kamokuMei
        binding.textKurasu.text = args.kurasuMei
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val realm = Realm.getDefaultInstance()

        //出席の存在を確認する
        val realmResult = realm.where<Syusseki>()
            .equalTo("kurasu_mei",args.kurasuMei)
            .equalTo("nen",args.nen)
            .equalTo("tsuki",args.tsuki)
            .equalTo("hi",args.hi)
            .equalTo("jikan",args.jikan)
            .findAll()

        val maxId = realm.where<Syusseki>().max("id")
        var nextId = (maxId?.toLong() ?: 0L) + 1L
        Log.d("realmSize" , realmResult.size.toString() )
        if( realmResult.size > 0){
            //出席がrealmに存在する
            realmResult.forEach {
                val syusseki = Syusseki(
                    it.id,
                    it.gakusei_id,
                    it.no,
                    it.gakusei_mei,
                    it.kurasu_mei,
                    it.tantou_account,
                    it.kamoku_mei,
                    it.nen,
                    it.tsuki,
                    it.hi,
                    it.jikan,
                    it.syusseki_code
                )
                nextId++
                syussekiList.add( syusseki )
                Log.d("存在する" , it.kamoku_mei )
            }
            existsSyussekiRealm = true

        }else {
            //出席がrealmに存在しない
            val realmResult = realm.where<Gakusei>().equalTo("kurasu_mei", args.kurasuMei).sort("no").findAll()
            realmResult.forEach {
                val syusseki = Syusseki(
                    nextId,
                    it.gakusei_id,
                    it.no,
                    it.gakusei_mei,
                    args.kurasuMei,
                    account.email,
                    args.kamokuMei,
                    args.nen,
                    args.tsuki,
                    args.hi,
                    args.jikan,
                    0)
                syussekiList.add(syusseki)
                nextId++
                Log.d("存在しない",args.kamokuMei)
                existsSyussekiRealm = false
            }
        }

        //出席入力用recyclerview
        val adapter = InputFragmentRecyclerViewAdapter( syussekiList )

        adapter.setItemOnClickListener { syusseki_code, position ->
            syussekiList[position].syusseki_code = syusseki_code
        }

        binding.recyclerViewInput.adapter = adapter
        binding.recyclerViewInput.layoutManager = LinearLayoutManager(context)

        //保存ボタンクリック時レルムに保存または更新する
        binding.saveButton.setOnClickListener {

            Log.d("exists" , existsSyussekiRealm.toString() )

            if( existsSyussekiRealm ){
                //すでにデータがあるので更新
                syussekiList.forEach{ it1 ->
                    realm.executeTransactionAsync{ it2 ->
                        val syusseki = it2.where<Syusseki>().equalTo("id",it1.id ).findFirst()
                        if( binding.editKamokuMei.text.length > 0 ) {
                            //syusseki!!.kamoku_mei = it1.kamoku_mei
                        //}else{
                            syusseki!!.kamoku_mei = binding.editKamokuMei.text.toString()
                        }
                        syusseki!!.syusseki_code = it1.syusseki_code
                    }
                }
                Snackbar.make(binding.root,"更新しました。",Snackbar.LENGTH_SHORT).show()
            }else{
                //まだデータが存在しないので、追加
                syussekiList.forEach{it1->
                    realm.executeTransactionAsync{ it2->
                        var syusseki = it2.createObject<Syusseki>(it1.id)
                        syusseki.gakusei_id = it1.gakusei_id
                        syusseki.no = it1.no
                        syusseki.gakusei_mei = it1.gakusei_mei
                        syusseki.kurasu_mei = it1.kurasu_mei
                        syusseki.tantou_account = it1.tantou_account
                        if( binding.editKamokuMei.text.length == 0 ) {
                            syusseki.kamoku_mei = it1.kamoku_mei
                        //}else{
                        //    syusseki.kamoku_mei = binding.editKamokuMei.text.toString()
                        }

                        //Log.d("realmkamokumei", syusseki.kamoku_mei )

                        syusseki.nen = it1.nen
                        syusseki.tsuki = it1.tsuki
                        syusseki.hi = it1.hi
                        syusseki.jikan = it1.jikan
                        syusseki.syusseki_code = it1.syusseki_code
                    }

                    Log.d("insert", it1.kamoku_mei )

                }
                //追加したので存在フラグをtrueにする
                existsSyussekiRealm = true
                Snackbar.make(binding.root,"追加しました",Snackbar.LENGTH_SHORT).show()

            }
        }
    }
}