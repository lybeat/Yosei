package cc.arturia.yosei.module.base.adapter

import cc.arturia.yosei.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Author: Arturia
 * Date: 2018/11/15
 */
class BaseTextAdapter(data: List<String>?) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_text, data) {

    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.tv_text, item)
    }
}