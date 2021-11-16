package tokyo.mstp015v.atendance

import io.realm.RealmObject

open class KurasuPermission (
    var id : Long = 0,
    var kurasu_mei : String = "",
    var tantou_account : String = ""
    ) : RealmObject()