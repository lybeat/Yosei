package cc.arturia.yosei.module.folder

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import cc.arturia.yosei.R
import cc.arturia.yosei.data.Folder
import cc.arturia.yosei.data.SpConfig
import cc.arturia.yosei.event.FolderEvent
import cc.arturia.yosei.event.FolderHideEvent
import cc.arturia.yosei.event.RxBus
import cc.arturia.yosei.module.base.BaseFragment
import cc.arturia.yosei.module.base.adapter.BaseTextAdapter
import cc.arturia.yosei.module.video.VideoListActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_folder.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import kotlin.properties.Delegates

/**
 * Author: Arturia
 * Date: 2018/11/7
 */
class FolderFragment : BaseFragment() {

    private var realm: Realm by Delegates.notNull()
    private var folderList: List<Folder>? = null
    private lateinit var adapter: FolderAdapter

    override fun getLayoutResId(): Int = R.layout.fragment_folder

    private lateinit var folderEditArray: Array<String>

    override fun initData() {
        realm = Realm.getDefaultInstance()
        folderList = realm.where(Folder::class.java).sort("timestamp", Sort.ASCENDING).findAll()
        folderEditArray = resources.getStringArray(R.array.folder_edit_array)
    }

    override fun initView() {
        adapter = FolderAdapter(activity!!, folderList)
        rv_folder.layoutManager = LinearLayoutManager(activity)
        rv_folder.adapter = adapter
    }

    override fun bindListener() {
        iv_add_folder.setOnClickListener { FolderActivity.launch(activity!!) }
        rv_folder.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                if (getString(R.string.app_name) == this@FolderFragment.adapter.data[position].name!!) {
                    showPsdDialog()
                } else {
                    VideoListActivity.launch(activity!!, this@FolderFragment.adapter.data[position].name!!)
                }
            }

            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                showEditDialog()
            }
        })
    }

    private fun showPsdDialog() {
        val hint: String = if (SpConfig.getLockPassword(activity!!).isNotEmpty()) {
            getString(R.string.input_password)
        } else {
            getString(R.string.setting_password)
        }
        MaterialDialog.Builder(activity!!)
                .input(hint, "", false
                ) { _, input ->
                    when {
                        input.toString() == SpConfig.getLockPassword(activity!!) ->
                            VideoListActivity.launch(activity!!, getString(R.string.app_name))
                        else -> {
                            Toast.makeText(activity!!, R.string.password_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .onPositive { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    private fun showEditDialog() {
        val root = LayoutInflater.from(activity).inflate(R.layout.dialog_list, null)
        val rvList = root.findViewById<View>(R.id.rv_list) as RecyclerView
        val textAdapter = BaseTextAdapter(folderEditArray.asList())
        rvList.adapter = textAdapter
        rvList.layoutManager = LinearLayoutManager(activity)
        rvList.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
            }
        })
        MaterialDialog.Builder(activity!!)
                .customView(root, false)
                .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.close()
    }

    override fun subscribeEvents(): Subscription? {
        return RxBus.instance.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { o ->
                    if (o is FolderEvent) {
                        insertFolder(o.path)
                        updateFolderList()
                    } else if (o is FolderHideEvent) {
                        updateFolderList()
                    }
                }
                .subscribe(RxBus.defaultSubscriber())
    }

    private fun updateFolderList() {
        folderList = realm.where(Folder::class.java).sort("timestamp", Sort.ASCENDING).findAll()
        adapter.setNewData(folderList)
    }

    private fun insertFolder(path: String) {
        val names = path.split("/")
        val folder = Folder()
        if (names.isNotEmpty()) {
            folder.name = names[names.size - 1]
        } else {
            folder.name = getString(R.string.no_name)
        }
        folder.path = path
        folder.cover = ""
        folder.timestamp = System.currentTimeMillis()
        realm.executeTransaction { realm.copyToRealmOrUpdate(folder) }
    }
}