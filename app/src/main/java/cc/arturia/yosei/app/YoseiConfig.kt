package cc.arturia.yosei.app

import cc.arturia.yosei.util.FileUtil

/**
 * Author: Arturia
 * Date: 2018/11/6
 */
object YoseiConfig {

    const val DIRECTORY = "Yosei"
    const val GLIDE_CACHE_SIZE = 100 * 1024 * 1024

    val ROOT = FileUtil.createRoot(DIRECTORY)
    val THUMB = FileUtil.createChild(ROOT, "thumb")
}