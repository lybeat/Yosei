package cc.arturia.yosei.module.video.presenter

import cc.arturia.yosei.data.Video
import rx.Observable

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
interface VideoContract {

    fun loadVideoList(): Observable<List<Video>>
}