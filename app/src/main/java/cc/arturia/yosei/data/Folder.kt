package cc.arturia.yosei.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Author: Arturia
 * Date: 2018/11/8
 */
open class Folder : RealmObject() {

    @PrimaryKey
    var name: String? = null
    var path: String? = null
    var cover: String? = null
    var timestamp: Long? = null
}