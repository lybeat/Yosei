package cc.arturia.yosei.widget.media

import android.content.Context
import cc.arturia.yosei.R
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.util.ColorUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Author: Arturia
 * Date: 2018/12/13
 */
class GroupAdapter(private val context: Context, videos: List<Video>)
    : BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_group, videos) {

    private var currentVideoName = ""

    override fun convert(helper: BaseViewHolder, item: Video) {
        if (currentVideoName == item.name) {
            helper.setTextColor(R.id.tv_video_name, ColorUtil.getColor(context, R.color.red))
        } else {
            helper.setTextColor(R.id.tv_video_name, ColorUtil.getColor(context, R.color.white))
        }
        helper.setText(R.id.tv_video_name, item.name)
    }

    fun setCurrentVideoName(name: String) {
        currentVideoName = name
    }
}