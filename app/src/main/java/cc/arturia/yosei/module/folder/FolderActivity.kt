package cc.arturia.yosei.module.folder

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import cc.arturia.yosei.R
import cc.arturia.yosei.event.FolderEvent
import cc.arturia.yosei.event.RxBus
import cc.arturia.yosei.module.base.ImmersiveActivity
import cc.arturia.yosei.util.FileUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.activity_folder.*
import java.io.File

/**
 * Author: Arturia
 * Date: 2018/6/6
 */
class FolderActivity : ImmersiveActivity() {

    private lateinit var adapter: FileAdapter
    private var fileList: ArrayList<File>? = null
    private var path: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_folder
    }

    override fun initData() {
        val storageList = FileUtil.getAvailableStorage(this)
        fileList = ArrayList()
        for (storage in storageList) {
            fileList!!.add(File(storage.path))
        }
        path = fileList!![0].path
    }

    override fun initView() {
        adapter = FileAdapter(fileList!!)
        rv_folder.adapter = adapter
        rv_folder.layoutManager = LinearLayoutManager(this)
    }

    override fun bindListener() {
        rv_folder.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                if (fileList!![position].isDirectory) {
                    openFolder(position)
                }
            }
        })
        tv_cancel.setOnClickListener { finish() }
        tv_confirm.setOnClickListener {
            RxBus.instance.post(FolderEvent(path!!))
            finish()
        }
    }

    private fun openFolder(position: Int) {
        path = fileList!![position].absolutePath
        fileList = FileUtil.getFileListByPath(path!!)
        adapter.setNewData(fileList)
    }

    private fun goBackFolder(): Boolean {
        val tempPath = File(path).parent
        if (TextUtils.isEmpty(tempPath) || tempPath == "/storage/emulated") return false
        path = tempPath
        fileList = FileUtil.getFileListByPath(path!!)
        adapter.setNewData(fileList)
        return true
    }

    override fun onBackPressed() {
        if (!goBackFolder()) {
            super.onBackPressed()
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent()
            intent.setClass(context, FolderActivity::class.java)
            context.startActivity(intent)
        }
    }
}