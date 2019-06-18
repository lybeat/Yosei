package cc.arturia.yosei.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
open class Video : RealmObject() {

    @PrimaryKey
    var name: String? = null
    var path: String? = null
    var format: String? = null
    var size: Long? = null
    var thumb: String? = null
    var progress: Long? = 0
    var duration: Long? = 0
    var timestamp: Long? = 0
    var hide: Boolean = false
}