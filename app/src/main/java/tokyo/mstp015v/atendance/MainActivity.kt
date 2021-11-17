package tokyo.mstp015v.atendance

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.realm.*
import io.realm.kotlin.where
import kotlinx.coroutines.*
import tokyo.mstp015v.atendance.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menu : Menu
    //private lateinit var credential : GoogleAccountCredential
    var sheet_id : String? = null
        get(){
            return field
        }
        set(value){
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate( layoutInflater )
        setContentView( binding.root )

        //レルムスキーマが変わった時にはいったん消すための設定
        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration( config )
        //val realm = Realm.getDefaultInstance(  )

        //プリファレンスにsheet_idが記録されているか確認する
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        sheet_id = pref.getString("SHEET_ID" ,null )
        if( sheet_id != null ) {
            Log.d("pref", sheet_id!!)
        }

        //ActionvarをToolbarにする
        setSupportActionBar( binding.toolbar )

        //ナビゲーションをdrawerと接続する
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost ) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navView,navController)
        NavigationUI.setupActionBarWithNavController(this,navController,binding.drawerLayout)

        //drawerにclickListenerを設定
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.itemMake-> {
                    val action = MainFragmentDirections.actionMainFragmentToMakeTopFragment()
                    binding.navHost.findNavController().navigate(action )
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.itemInput-> {
                    val action = MainFragmentDirections.actionMainFragmentToInputListFragment()
                    binding.navHost.findNavController().navigate(action)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.itemRestore-> {
                    val action = MainFragmentDirections.actionMainFragmentToRestoreFragment()
                    binding.navHost.findNavController().navigate(action)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.itemBackup->{
                    val action = MainFragmentDirections.actionMainFragmentToBackupFragment()
                    binding.navHost.findNavController().navigate(action)
                    binding.drawerLayout.closeDrawers()
                    true
                }else->{
                    true
                }
            }
        }

        //サインインチェック
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if( account != null ) {
            //navheaderにDisplayNameを設定
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textNavHeaderEmail).text = account.displayName
            //インプットとアウトプットをtrueにする
            binding.navView.menu.findItem(R.id.itemInput).isEnabled = true
            binding.navView.menu.findItem(R.id.itemOutput).isEnabled = true
            binding.navView.menu.findItem(R.id.itemBackup).isEnabled = true
            binding.navView.menu.findItem(R.id.itemRestore).isEnabled = true
            binding.navView.menu.findItem(R.id.itemMake).isEnabled = true
        }else{
            //navheaderにDisplayNameを設定
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textNavHeaderEmail).text = "サインインしてません"
        }

    }

    //DrawerとNavigationの連携に必要なコールバック
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHost)
        return NavigationUI.navigateUp(navController,binding.drawerLayout)
    }

    //引数のジェネリッククラスでRealmから次のIDを取得する
    inline fun <reified T : RealmObject> getNextId(realm : Realm ) : Long{
        val maxId = realm.where<T>().max("id")
        val nextId = (maxId?.toLong() ?: 0L ) + 1L
        return nextId
    }

    //option_menuの作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate( R.menu.option_menu ,menu )

        this.menu = menu!!
        //SignInチェック
        val account = GoogleSignIn.getLastSignedInAccount( applicationContext )
        if( account != null ){
            buttonSignIn( account )
        }else{
            buttonSignOut()
        }

        return true
    }

    //GoogleSignInのコールバック
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d("launcher","launcher")
        if( it.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            val account = GoogleSignIn.getLastSignedInAccount( applicationContext )
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext,
                Collections.singleton("https://www.googleapis.com/auth/spreadsheets")
            )
            credential?.setSelectedAccount( account?.account )
            Log.d("signin",task.isSuccessful.toString() )
            //サインインに成功
            if( task.isSuccessful ){
                buttonSignIn( account )
                credential?.selectedAccount = account.account
                //atendance シートの確認

                //SheardPriferenceを確認しSpreadSheetの存在を確認する
                val pref = PreferenceManager.getDefaultSharedPreferences(this)
                var id :String? = pref.getString("SHEET_ID",null)

                if( id == null ){
                    //存在しないのでSheetを作る
                    createSheets( )

                }else{
                    this.sheet_id = id
                    Snackbar.make(binding.root,"サインインしました",Snackbar.LENGTH_SHORT).show()
                    binding.progressBar2.visibility = ProgressBar.INVISIBLE
                }
            }
        }
    }

    //spreadsheet新規作成
    fun createSheets() {
        //coroutine
        MainScope().launch{
            val realm = Realm.getDefaultInstance()
            val id = getNextId<SheetId>(realm)
            realm.close()
            val ymd = getYmd()

            val s_id = async( Dispatchers.Default ){
                val account = GoogleSignIn.getLastSignedInAccount( applicationContext )
                //OAuth2のcredentialを作る
                val credential = GoogleAccountCredential.usingOAuth2(
                    applicationContext,
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

                var sheets = Spreadsheet().apply{
                    setProperties(
                        SpreadsheetProperties().setTitle("atendance_${id}_${ymd}")
                    )
                    sheets = list
                }

                sheets = sheetsService.spreadsheets().create(sheets).execute()

                //sheet_id = sheets.spreadsheetId

                //セルに値を表示する
                val valuesList = mutableMapOf<String,List<List<Any>>>()
                valuesList.put("gakusei",Arrays.asList(Arrays.asList("id","gakusei_id","no","gakusei_mei","kurasu_mei")))
                valuesList.put("risyuu",Arrays.asList(Arrays.asList("id","kurasu_mei","kamoku_mei","youbi","jikan")))
                valuesList.put("syusseki",Arrays.asList(Arrays.asList("id","gakusei_id","no","gakusei_mei","kurasu_mei","tantou_account","kamoku_mei","nen","tsuki","hi","jikan","syusseki_code")))
                valuesList.put("kurasupermission",Arrays.asList(Arrays.asList("id","kurasu_mei","tantou_account")))

                valuesList.forEach{
                    val result : AppendValuesResponse = sheetsService.spreadsheets().values()
                        .append(sheets.spreadsheetId,"${it.key}!A:E",ValueRange().setValues( it.value ))
                        .setValueInputOption("RAW")
                        .execute()
                }

                return@async sheets.spreadsheetId
            }.await()

            //
            sheet_id = s_id
            Snackbar.make(binding.root,"${sheet_id!!.substring(0,5)}…を作成しました",Snackbar.LENGTH_SHORT).show()
            binding.progressBar2.visibility = ProgressBar.INVISIBLE

            //プリファレンスに書き込む
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            pref.edit().apply{
                this.putString("SHEET_ID",s_id!! )
            }.commit()

            //sheetid realmに追加する
            val sheetid_item = SheetId( id,s_id!!,ymd)
            realm.beginTransaction()
            realm.insert( sheetid_item )
            realm.commitTransaction()

        }
    }

    //右側メニューの選択時コールバック
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //サインインオプションの作成
        val gso = GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes( Scope("https://www.googleapis.com/auth/spreadsheets"))
            .build()

        val client = GoogleSignIn.getClient( this, gso )

        when(item.itemId){
            R.id.item_sign_in_out->{
                if(item.title=="SignIn"){
                    binding.progressBar2.visibility = ProgressBar.VISIBLE
                    val intent = client.signInIntent
                    launcher.launch( intent )

                }else if( item.title=="SignOut"){
                    client.signOut()
                    //ボタン系を一括設定
                    buttonSignOut()
                    //MainFragmentに戻る
                    findNavController(R.id.navHost).navigate( R.id.grobal_action_to_main )

                }
            }
            //R.id.item_restore->{
            //    restore()
            //}

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    //サインインした時の一括設定
    fun buttonSignIn(account : GoogleSignInAccount){
        menu.findItem(R.id.item_sign_in_out).title = "SignOut"
        //menu.findItem(R.id.item_restore).isEnabled = true
        //menu.findItem(R.id.item_backup).isEnabled = true
        binding.navView.menu.findItem(R.id.itemInput).isEnabled = true
        binding.navView.menu.findItem(R.id.itemOutput).isEnabled = true
        binding.navView.menu.findItem(R.id.itemRestore).isEnabled = true
        binding.navView.menu.findItem(R.id.itemBackup).isEnabled = true
        //DrawerのHeader
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textNavHeaderEmail).text = account.displayName

    }

    //サインアウトした時の一括設定
    fun buttonSignOut(){
        menu.findItem(R.id.item_sign_in_out).title = "SignIn"
        //menu.findItem(R.id.item_restore).isEnabled = false
        //menu.findItem(R.id.item_backup).isEnabled = false
        binding.navView.menu.findItem(R.id.itemInput).isEnabled = false
        binding.navView.menu.findItem(R.id.itemOutput).isEnabled = false
        binding.navView.menu.findItem(R.id.itemRestore).isEnabled = false
        binding.navView.menu.findItem(R.id.itemBackup).isEnabled = false
        //DrawerのHeader
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textNavHeaderEmail).text = "サインインしてません"

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

}
