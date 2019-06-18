package cc.arturia.yosei.app

import android.app.Application
import io.realm.Realm

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class Yosei : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        Realm.init(this)
    }

    companion object {

        lateinit var instance: Yosei
    }
}