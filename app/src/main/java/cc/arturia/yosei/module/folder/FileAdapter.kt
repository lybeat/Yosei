package cc.arturia.yosei.module.folder

import cc.arturia.yosei.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File

/**
 * Author: Arturia
 * Date: 2018/6/6
 */
class FileAdapter(data: List<File>?) : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_file, data) {

    override fun convert(helper: BaseViewHolder, item: File) {
        helper.setText(R.id.tv_name, item.name)
    }
}