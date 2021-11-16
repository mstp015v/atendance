package tokyo.mstp015v.atendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Sheet
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tokyo.mstp015v.atendance.databinding.FragmentRestoreBinding
import java.util.*


class RestoreFragment : Fragment() {
    private var _binding : FragmentRestoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRestoreBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        val result = realm.where<SheetId>().findAll()
        Log.d("restore","before loop")
        result.forEach {
            Log.d("restore",it.sheetId)
        }
        val adapter = SheetIdRealmAdapter( result )
        adapter.setOnItemClickListener {
            restore( it!! )
        }
        binding.recyclerViewRestore.adapter = adapter
        binding.recyclerViewRestore.layoutManager = LinearLayoutManager(this.context )
        realm.close()

    }

    //リストア
    fun restore( sheet_id : String ){

        MainScope().launch{
            binding.restoreProgressBar.visibility = android.widget.ProgressBar.VISIBLE
            withContext(Dispatchers.Default){
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

                val res1 = sheetsService.spreadsheets().values().get(sheet_id,"gakusei!A2:E100").execute()
                val val1 = res1.getValues()
                val res2 = sheetsService.spreadsheets().values().get(sheet_id,"risyuu!A2:F100").execute()
                val val2 = res2.getValues()
                val res3 = sheetsService.spreadsheets().values().get(sheet_id,"syusseki!A2:L1000").execute()
                val val3 = res3.getValues()
                val res4 = sheetsService.spreadsheets().values().get(sheet_id,"kurasupermission!A2:C100").execute()
                val val4 = res4.getValues()
                val realm = Realm.getDefaultInstance()
                //リストア時データはいったんすべて消す
                val gakusei = realm.where<Gakusei>().findAll()
                val risyuu = realm.where<Risyuu>().findAll()
                val syusseki = realm.where<Syusseki>().findAll()
                val kurasupermission = realm.where<KurasuPermission>().findAll()
                realm.executeTransaction{
                    gakusei.deleteAllFromRealm()
                    risyuu.deleteAllFromRealm()
                    syusseki.deleteAllFromRealm()
                    kurasupermission.deleteAllFromRealm()
                }

                //追加する
                val1?.forEach {
                    Log.d("coroutine" , it[0].toString() )
                    val id = Integer.parseInt( it[0] as String ).toLong()
                    val gakusei_id = it[1] as String
                    val no = Integer.parseInt(it[2] as String)
                    val gakusei_mei = it[3] as String
                    val kurasu_mei = it[4] as String
                    val gakusei_item = Gakusei( id ,gakusei_id,no,gakusei_mei,kurasu_mei)
                    realm.beginTransaction()
                    realm.insert( gakusei_item )
                    realm.commitTransaction()
                }

                val2?.forEach{
                    val id = Integer.parseInt( it[0] as String ).toLong()
                    val kurasu_mei = it[1] as String
                    val kamoku_mei = it[2] as String
                    val youbi = it[3] as String
                    val jikan = Integer.parseInt( it[4] as String )

                    val risyuu_item = Risyuu(id,kurasu_mei,kamoku_mei,youbi,jikan)
                    realm.beginTransaction()
                    realm.insert( risyuu_item )
                    realm.commitTransaction()
                }

                val3?.forEach{
                    val id = Integer.parseInt( it[0] as String).toLong()
                    val gakusei_id = it[1] as String
                    val no = Integer.parseInt( it[2] as String )
                    val gakusei_mei = it[3] as String
                    val kurasu_mei = it[4] as String
                    val tantou_account = it[5] as String
                    val kamoku_mei = it[6] as String
                    val nen = Integer.parseInt( it[7] as String )
                    val tsuki = Integer.parseInt( it[8] as String )
                    val hi = Integer.parseInt( it[9] as String )
                    val jikan = Integer.parseInt( it[10] as String )
                    val syusseki_code = Integer.parseInt( it[10] as String )

                    val syusseki_item = Syusseki(
                        id,gakusei_id,no,gakusei_mei,kurasu_mei,tantou_account,kamoku_mei,nen,tsuki,hi,jikan,syusseki_code
                    )

                    realm.beginTransaction()
                    realm.insert( syusseki_item )
                    realm.commitTransaction()

                }

                val4?.forEach{
                    val id = Integer.parseInt( it[0] as String).toLong()
                    val kurasu_mei = it[1] as String
                    val tantou_account = it[2] as String
                    val kurasu_item = KurasuPermission(
                        id,kurasu_mei,tantou_account
                    )

                    realm.beginTransaction()
                    realm.insert( kurasu_item )
                    realm.commitTransaction()

                }
                realm.close()
            }
            //Toast.makeText(context,"リストア終了",Toast.LENGTH_SHORT).show()
            binding.restoreProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
            Snackbar.make(binding.root,"リストアが終わりました",Snackbar.LENGTH_SHORT).show()
        }
    }

}