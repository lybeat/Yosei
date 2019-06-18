package cc.arturia.yosei.module.video

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import cc.arturia.yosei.R
import cc.arturia.yosei.app.GlideApp
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.module.base.adapter.BaseSelectAdapter
import cc.arturia.yosei.util.DateUtil
import cc.arturia.yosei.util.UnitUtil
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Author: Arturia
 * Date: 2018/11/8
 */
class VideoGridAdapter(private val context: Context, data: List<Video>?)
    : BaseSelectAdapter<Video, BaseViewHolder>(R.layout.item_video_grid, data) {

    override fun convert(k: BaseViewHolder, t: Video) {
        k.setText(R.id.tv_name, t.name)
        k.setText(R.id.tv_time, String.format("%s/%s", DateUtil.stringForTime(t.progress!!), DateUtil.stringForTime(t.duration!!)))
        val ivThumb = k.getView<ImageView>(R.id.iv_thumb)
        val multi = MultiTransformation(CenterCrop(), RoundedCorners(UnitUtil.dp2px(context, 10f)))
        GlideApp.with(context)
                .asBitmap()
                .load(t.thumb)
                .transform(multi)
                .into(ivThumb)
        val box = k.getView<CheckBox>(R.id.cb_select)
        if (getMode() == BaseSelectAdapter.MODE_SELECT) {
            box.visibility = View.VISIBLE
            box.isChecked = select[data.indexOf(t)]
        } else {
            box.visibility = View.GONE
        }
    }
}