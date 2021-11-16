package tokyo.mstp015v.atendance

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Syusseki(
    @PrimaryKey
    var id : Long = 0,
    var gakusei_id : String = "",
    var no : Int = 0,
    var gakusei_mei : String = "",
    var kurasu_mei : String = "",
    var tantou_account : String = "",
    var kamoku_mei:String = "",
    var nen : Int = 0,
    var tsuki : Int = 0,
    var hi : Int = 0,
    var jikan : Int = 0,
    var syusseki_code : Int = 0
) : RealmObject()