package cc.arturia.yosei.module.base.mvp

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
interface BaseView<in T> {

    fun setPresenter(presenter: T)

    fun showLoading()

    fun hideLoading()

    fun handleError(error: Throwable)
}