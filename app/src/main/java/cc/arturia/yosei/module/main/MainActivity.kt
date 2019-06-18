package cc.arturia.yosei.module.main

import android.content.Context
import android.content.Intent
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.widget.Toast
import cc.arturia.yosei.R
import cc.arturia.yosei.module.base.FragmentBackHelper
import cc.arturia.yosei.module.base.ImmersiveActivity
import cc.arturia.yosei.module.base.MainTabAdapter
import cc.arturia.yosei.module.folder.FolderFragment
import cc.arturia.yosei.module.settings.SettingsFragment
import cc.arturia.yosei.module.video.VideoActivity
import cc.arturia.yosei.module.video.VideoListFragment
import cc.arturia.yosei.util.StatusBarUtil
import cc.arturia.yosei.widget.magictab.Tab
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : ImmersiveActivity(), DrawerLayout.DrawerListener {

    private var backTime: Long = 0
    private var tabs: MutableList<Tab>? = null

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun initData() {
        tabs = ArrayList()
        tabs!!.add(Tab(VideoListFragment(), R.drawable.ic_video_normal, R.drawable.ic_video_selected))
        tabs!!.add(Tab(FolderFragment(), R.drawable.ic_folder_normal, R.drawable.ic_folder_selected))
        tabs!!.add(Tab(SettingsFragment(), R.drawable.ic_settings_normal, R.drawable.ic_settings_selected))
    }

    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.navigation_view, NavigationFragment()).commit()
        drawer_layout.addDrawerListener(this)

        val tabAdapter = MainTabAdapter(supportFragmentManager, tabs!!)
        vp_main.adapter = tabAdapter
        vp_main.offscreenPageLimit = 2
        bottom_tab.setViewPager(vp_main)
    }

    override fun bindListener() {
    }

    override fun onBackPressed() {
        if (!FragmentBackHelper.handleBackPressed(this)) {
            if (System.currentTimeMillis() - backTime > 1500) {
                backTime = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, "再按一次退出", Toast.LENGTH_SHORT).show()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        StatusBarUtil.statusBarLightMode(this)
    }

    override fun onDrawerOpened(drawerView: View) {
        StatusBarUtil.statusBarDarkMode(this)
    }

    companion object {

        fun launch(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
