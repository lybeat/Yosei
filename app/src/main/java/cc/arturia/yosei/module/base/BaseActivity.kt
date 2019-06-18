package cc.arturia.yosei.module.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
abstract class BaseActivity : AppCompatActivity() {

    private var compositeSubscription: CompositeSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(getLayoutResId())

        initData()
        initView()
        bindListener()

        addSubscription(subscribeEvents())
    }

    protected abstract fun getLayoutResId() : Int

    protected abstract fun initData()

    protected abstract fun initView()

    protected abstract fun bindListener()

    override fun onDestroy() {
        super.onDestroy()
        if (compositeSubscription != null) {
            compositeSubscription!!.clear()
        }
    }

    private fun addSubscription(subscription: Subscription?) {
        if (subscription == null) {
            return
        }
        if (compositeSubscription == null) {
            compositeSubscription = CompositeSubscription()
        }
        compositeSubscription!!.add(subscription)
    }

    protected open fun subscribeEvents(): Subscription? {
        return null
    }
}