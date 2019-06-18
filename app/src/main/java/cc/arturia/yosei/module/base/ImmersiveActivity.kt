package cc.arturia.yosei.module.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import cc.arturia.yosei.R
import cc.arturia.yosei.util.StatusBarUtil

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
abstract class ImmersiveActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ImmersiveTheme)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        StatusBarUtil.statusBarLightMode(this)
    }
}
