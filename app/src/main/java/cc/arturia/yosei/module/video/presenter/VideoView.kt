package cc.arturia.yosei.module.video.presenter

import cc.arturia.yosei.data.Video
import cc.arturia.yosei.module.base.mvp.BaseView

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
interface VideoView : BaseView<VideoPresenter> {

    fun onVideoListLoaded(videoList: List<Video>)
}