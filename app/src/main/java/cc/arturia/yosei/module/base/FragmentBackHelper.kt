package cc.arturia.yosei.module.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
object FragmentBackHelper {

    fun handleBackPressed(fragment: Fragment): Boolean {
        return handleBackPressed(fragment.childFragmentManager)
    }

    fun handleBackPressed(activity: AppCompatActivity): Boolean {
        return handleBackPressed(activity.supportFragmentManager)
    }

    private fun handleBackPressed(fragmentManager: FragmentManager): Boolean {
        val fragments = fragmentManager.fragments ?: return false

        for (i in fragments.indices.reversed()) {
            if (isFragmentBackHandled(fragments[i])) {
                return true
            }
        }
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    /**
     * 判断Fragment是否处理了Back键
     *
     * @return 如果处理了back键则返回 **true**
     */
    private fun isFragmentBackHandled(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.isVisible
                && fragment.userVisibleHint // for ViewPager

                && fragment is OnFragmentBackListener
                && (fragment as OnFragmentBackListener).onBackPressed())
    }

    interface OnFragmentBackListener {

        fun onBackPressed(): Boolean
    }
}
