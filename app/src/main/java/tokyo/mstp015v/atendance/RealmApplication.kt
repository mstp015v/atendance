package tokyo.mstp015v.atendance

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmApplication : Application() {
    override fun onCreate(){
        super.onCreate()
        Realm.init( this )

        //UIスレッドからの書き込み可能+スキーマが変わったらrealmを削除
        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration( config )
    }
}