package cc.arturia.yosei.module.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import cc.arturia.yosei.widget.magictab.MagicTab
import cc.arturia.yosei.widget.magictab.Tab

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class MainTabAdapter(fm: FragmentManager, private val tabs: List<Tab>) : FragmentPagerAdapter(fm), MagicTab.TabIcon {

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        return tabs[position].fragment
    }

    override fun getCount(): Int {
        return tabs.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabs[position].title
    }

    override fun getPageNormalIconIdDay(position: Int): Int {
        return tabs[position].normalIconIdDay
    }

    override fun getPageNormalIconIdNight(position: Int): Int {
        return tabs[position].normalIconIdNight
    }

    override fun getPagePressedIconId(position: Int): Int {
        return tabs[position].pressedIconId
    }
}
