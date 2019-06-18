package cc.arturia.yosei.module.base.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
open class BaseSelectAdapter<T, K : BaseViewHolder>(layoutResId: Int, data: List<T>?) : BaseQuickAdapter<T, K>(layoutResId, data) {

    private var mode: Int = 0
    // 存放每一个item所对应的状态(是否被选择)
    lateinit var select: BooleanArray
        protected set

    val isAllSelect: Boolean
        get() {
            for (aSelect in select) {
                if (!aSelect) {
                    return false
                }
            }
            return true
        }

    override fun convert(k: K, t: T) {}

    fun update() {
        notifyDataSetChanged()
        select = BooleanArray(data.size)
    }

    fun setChildSelect(position: Int) {
        select[position] = !select[position]
        notifyDataSetChanged()
    }

    fun setAllSelect() {
        for (i in select.indices) {
            select[i] = true
        }
        notifyDataSetChanged()
    }

    fun clearAllSelect() {
        for (i in select.indices) {
            select[i] = false
        }
        notifyDataSetChanged()
    }

    fun setMode(mode: Int) {
        this.mode = mode
        update()
    }

    fun getMode(): Int {
        return mode
    }

    companion object {

        // 正常模式
        const val MODE_NORMAL = 0
        // 选择模式
        const val MODE_SELECT = 1
    }
}
