package cc.arturia.yosei.module.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
abstract class BaseFragment : Fragment() {

    private var compositeSubscription: CompositeSubscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(getLayoutResId(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initView()
        bindListener()

        addSubscription(subscribeEvents())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (compositeSubscription != null) {
            compositeSubscription!!.clear()
        }
    }

    protected abstract fun getLayoutResId(): Int

    protected abstract fun initData()

    protected abstract fun initView()

    protected abstract fun bindListener()

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