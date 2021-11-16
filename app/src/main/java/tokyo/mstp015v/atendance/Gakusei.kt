package tokyo.mstp015v.atendance

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Gakusei(
    @PrimaryKey
    var id : Long = 0,   //オートナンバー
    var gakusei_id : String = "",
    var no:Int = 0,
    var gakusei_mei : String = "",
    var kurasu_mei : String = "",
) : RealmObject()