package cc.arturia.yosei.module.video.presenter

import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class VideoPresenter(private var view: VideoView) : IVideoPresenter {

    private val dataSource: VideoDataSource = VideoDataSource.get()
    private val compositeSubscription: CompositeSubscription = CompositeSubscription()

    init {
        this.view.setPresenter(this)
    }

    override fun subscribe() {
    }

    override fun unsubscribe() {
        compositeSubscription.clear()
    }

    override fun loadVideoList() {
        view.showLoading()
        val subscription = dataSource.loadVideoList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.onVideoListLoaded(it) }, { view.handleError(it)}, { view.hideLoading() })
        compositeSubscription.add(subscription)
    }
}