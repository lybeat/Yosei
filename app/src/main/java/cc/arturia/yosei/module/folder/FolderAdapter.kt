package cc.arturia.yosei.module.folder

import android.content.Context
import android.widget.ImageView
import cc.arturia.yosei.R
import cc.arturia.yosei.app.GlideApp
import cc.arturia.yosei.data.Folder
import cc.arturia.yosei.util.UnitUtil
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Author: Arturia
 * Date: 2018/11/8
 */
class FolderAdapter(private val context: Context, data: List<Folder>?)
    : BaseQuickAdapter<Folder, BaseViewHolder>(R.layout.item_folder, data) {

    override fun convert(helper: BaseViewHolder, item: Folder) {
        helper.setText(R.id.tv_name, item.name)
        val ivCover = helper.getView<ImageView>(R.id.iv_cover)
        val multi = MultiTransformation(CenterCrop(), RoundedCorners(UnitUtil.dp2px(context, 10f)))
        if (item.cover!!.isNotEmpty()) {
            GlideApp.with(context)
                    .asBitmap()
                    .load(item.cover)
                    .transform(multi)
                    .into(ivCover)
        } else {
            GlideApp.with(context)
                    .asBitmap()
                    .load(R.drawable.bg_transparent_fillet)
                    .transform(multi)
                    .into(ivCover)
        }
    }
}