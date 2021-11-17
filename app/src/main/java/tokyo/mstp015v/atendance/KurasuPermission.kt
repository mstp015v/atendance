package tokyo.mstp015v.atendance

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class KurasuPermission (
    @PrimaryKey
    var id : Long = 0,
    var kurasu_mei : String = "",
    var tantou_account : String = ""
    ) : RealmObject()