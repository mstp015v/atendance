package tokyo.mstp015v.atendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.realm.Realm
import io.realm.RealmObject
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tokyo.mstp015v.atendance.databinding.FragmentBackupBinding
import java.util.*


class BackupFragment : Fragment() {
    private var _binding : FragmentBackupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBackupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        val result = realm.where<SheetId>().findAll()

        //シートのバックアップ履歴を表示するRecyclerView
        val adapter = SheetIdRealmAdapter( result )
        binding.recyclerViewBackup.adapter = adapter
        binding.recyclerViewBackup.layoutManager = LinearLayoutManager(context)
        realm.close()

        //バックアップをとる
        binding.textNewBackup.setOnClickListener{
            backupSheets( null )
        }

    }
    //引数のジェネリッククラスでRealmから次のIDを取得する
    inline fun <reified T : RealmObject> getNextId(realm : Realm ) : Long{
        val maxId = realm.where<T>().max("id")
        val nextId = (maxId?.toLong() ?: 0L ) + 1L
        return nextId
    }
    //現在の日付をyyyymmddの形式で取得する
    fun getYmd():String{
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)

        var ymd = y.toString()
        if( m < 10 ){
            ymd = ymd + "0" + m.toString()
        }else{
            ymd = ymd + m.toString()
        }
        if( d< 10 ){
            ymd = ymd + "0" + d.toString()
        }else{
            ymd = ymd + d.toString()
        }

        return ymd
    }

    //spreadsheet新規作成
    fun backupSheets( backupsheetid : String? ) {
        binding.progressBar.visibility = android.widget.ProgressBar.VISIBLE
        //coroutine
        MainScope().launch{
            val realm = Realm.getDefaultInstance()
            val gakusei = realm.where<Gakusei>().findAll()
            val risyuu = realm.where<Risyuu>().findAll()
            val syusseki = realm.where<Syusseki>().findAll()
            val kurasupermission = realm.where<KurasuPermission>().findAll()

            val id = getNextId<SheetId>(realm)
            val ymd = getYmd()
            val account = GoogleSignIn.getLastSignedInAccount( context )
            //OAuth2のcredentialを作る
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton("https://www.googleapis.com/auth/spreadsheets")
            )
            credential?.setSelectedAccount( account?.account )
            val sheetsService = Sheets.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("atendanceapp").build()

            //シート名
            val sheetNames = arrayListOf("gakusei","risyuu","syusseki","kurasupermission")
            val list = mutableListOf<Sheet>()
            sheetNames.forEach{
                val sheet = Sheet().setProperties(
                    SheetProperties().setTitle(it)
                )
                list.add( sheet )
            }
            var retsheetid : String? = null
            val valuesList = mutableMapOf<String,List<List<Any>>>()

            if(backupsheetid==null){
                //atendance sheetを新規作成する場合(realmの内容を書き込む)
                //セルに値を表示する
                valuesList.put("gakusei", mutableListOf(mutableListOf("id","gakusei_id","no","gakusei_mei","kurasu_mei")))
                gakusei.forEach{
                    (valuesList.get("gakusei") as ArrayList).add(mutableListOf(it.id,it.gakusei_id,it.no,it.gakusei_mei,it.kurasu_mei))
                }
                valuesList.put("risyuu",mutableListOf(mutableListOf("id","kurasu_mei","kamoku_mei","youbi","jikan")))
                risyuu.forEach {
                    (valuesList.get("risyuu") as ArrayList).add(mutableListOf(it.id,it.kurasu_mei,it.kamoku_mei,it.youbi,it.jikan))
                }
                valuesList.put("syusseki",mutableListOf(mutableListOf(
                    "id",
                    "gakusei_id",
                    "no",
                    "gakusei_mei",
                    "kurasu_mei",
                    "tantou_account",
                    "kamoku_mei",
                    "nen",
                    "tsuki",
                    "hi",
                    "jikan",
                    "syusseki_code")))
                syusseki.forEach{
                    (valuesList.get("syusseki")as ArrayList).add(mutableListOf(
                        it.id,
                        it.gakusei_id,
                        it.no,
                        it.gakusei_mei,
                        it.kurasu_mei,
                        it.tantou_account,
                        it.kamoku_mei,
                        it.nen,
                        it.tsuki,it.hi,it.jikan,it.syusseki_code))
                }
                valuesList.put("kurasupermission", mutableListOf(mutableListOf("id","kurasu_mei","tantou_account")))
                kurasupermission.forEach {
                    (valuesList.get("kurasupermission") as ArrayList).add(mutableListOf(it.id,it.kurasu_mei,it.tantou_account))
                }

                //非同期通信開始
                val s_id = async(Dispatchers.Default ){
                    var sheets = Spreadsheet().apply{
                        setProperties(
                            SpreadsheetProperties().setTitle("atendance_${id}_${ymd}")
                        )
                        sheets = list
                    }
                    sheets = sheetsService.spreadsheets().create(sheets).execute()


                    valuesList.forEach {
                        //Log.d("key","${it.key},${it.value}")
                        //Log.d("value","${ValueRange().setValues(it.value)}")

                        val result: AppendValuesResponse = sheetsService.spreadsheets().values()
                            .append(sheets.spreadsheetId,
                                "${it.key}!A:L",
                                ValueRange().setValues(it.value))
                            .setValueInputOption("RAW")
                            .execute()

                    }
                    return@async sheets.spreadsheetId
                }.await()
                retsheetid = s_id
                binding.progressBar.visibility = android.widget.ProgressBar.INVISIBLE
            }else{
                //atendance sheetを更新する場合
                //非同期通信開始
                val s_id = async( Dispatchers.Default ){
                    val res1 = sheetsService.spreadsheets().values().get(backupsheetid,"syusseki!A2:K1000").execute()
                    val val1 = res1.getValues()
                    val syusseki = realm.where<Syusseki>().sort("id").findAll()
                    var i = 0
                    var j = 0

                    while( val1.size > i && syusseki.size > j ){

                        if( (val1[i][0] as Long) == syusseki[j]!!.id){
                            //idが一致している
                            val1[i][6] = syusseki[j]!!.kamoku_mei
                            val1[i][11] = syusseki[j]!!.syusseki_code
                            i++
                            j++
                        }else if( (val1[i][0] as Long) > syusseki[j]!!.id ){
                            //sheetにないがrealmにあるので追加
                            val list = mutableListOf<Any>()
                            list.add(syusseki[j]!!.id)
                            list.add(syusseki[j]!!.gakusei_id)
                            list.add(syusseki[j]!!.no)
                            list.add(syusseki[j]!!.gakusei_mei)
                            list.add(syusseki[j]!!.kurasu_mei)
                            list.add(syusseki[j]!!.tantou_account)
                            list.add(syusseki[j]!!.kamoku_mei)
                            list.add(syusseki[j]!!.nen)
                            list.add(syusseki[j]!!.tsuki)
                            list.add(syusseki[j]!!.hi)
                            list.add(syusseki[j]!!.jikan)
                            list.add(syusseki[j]!!.syusseki_code)
                            val1.add( list )
                            j++
                        }
                    }
                    while( syusseki.size > j ){
                        val list = mutableListOf<Any>()
                        list.add(syusseki[j]!!.id)
                        list.add(syusseki[j]!!.gakusei_id)
                        list.add(syusseki[j]!!.no)
                        list.add(syusseki[j]!!.gakusei_mei)
                        list.add(syusseki[j]!!.kurasu_mei)
                        list.add(syusseki[j]!!.tantou_account)
                        list.add(syusseki[j]!!.kamoku_mei)
                        list.add(syusseki[j]!!.nen)
                        list.add(syusseki[j]!!.tsuki)
                        list.add(syusseki[j]!!.hi)
                        list.add(syusseki[j]!!.jikan)
                        list.add(syusseki[j]!!.syusseki_code)
                        val1.add( list )
                        j++
                    }

                    return@async backupsheetid

                }.await()
                retsheetid = s_id
            }

            //MainActivityのsheetidに書き込む
            (activity as? MainActivity)?.sheet_id = retsheetid

            //プリファレンスに書き込む
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            pref.edit().apply{
                this.putString("SHEET_ID",retsheetid!! )
            }.commit()

            //sheetid realmに追加する
            val sheetid_item = SheetId( id,retsheetid!!,ymd)
            realm.beginTransaction()
            realm.insert( sheetid_item )
            realm.commitTransaction()

            Snackbar.make(binding.root,"バックアップ終了",Snackbar.LENGTH_SHORT).show()
        }
    }

}