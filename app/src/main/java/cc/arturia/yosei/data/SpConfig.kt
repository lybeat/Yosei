package cc.arturia.yosei.data

import android.content.Context

/**
 * Author: Arturia
 * Date: 2018/11/14
 */
object SpConfig {

    private const val SP_CONFIG = "sp_config"
    private const val KEY_VIDEO_LIST_STYLE = "key_video_list_style"
    private const val KEY_HIDE_FILE = "key_hide_file"
    private const val KEY_LOCK_PASSWORD = "key_lock_password"

    fun setVideoListStyle(context: Context, boolean: Boolean) {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(KEY_VIDEO_LIST_STYLE, boolean)
        editor.apply()
    }

    fun getVideoListStyle(context: Context): Boolean {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_VIDEO_LIST_STYLE, true)
    }

    fun setFileHide(context: Context, boolean: Boolean) {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(KEY_HIDE_FILE, boolean)
        editor.apply()
    }

    fun getFileHide(context: Context): Boolean {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_HIDE_FILE, false)
    }

    fun setLockPassword(context: Context, password: String) {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(KEY_LOCK_PASSWORD, password)
        editor.apply()
    }

    fun getLockPassword(context: Context): String {
        val sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE)
        return sp.getString(KEY_LOCK_PASSWORD, "")
    }
}