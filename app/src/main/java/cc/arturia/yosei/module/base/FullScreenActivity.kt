package cc.arturia.yosei.module.base

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import cc.arturia.yosei.R

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
abstract class FullScreenActivity : BaseActivity() {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.FullScreenTheme)
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
    }
}
