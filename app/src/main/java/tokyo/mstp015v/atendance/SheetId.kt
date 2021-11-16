package tokyo.mstp015v.atendance

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SheetId(
    @PrimaryKey
    var id :Long = 0,
    var sheetId : String = "",
    var ymd : String = ""
) :RealmObject()