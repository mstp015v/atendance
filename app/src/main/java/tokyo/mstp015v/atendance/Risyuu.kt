package tokyo.mstp015v.atendance

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Risyuu(
    @PrimaryKey
    var id : Long = 0,   //オートナンバー
    var kurasu_mei :String = "",
    var kamoku_mei : String = "",
    var youbi : String = "",
    var jikan : Int = 0,
) : RealmObject()