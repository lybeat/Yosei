package cc.arturia.yosei.module.video

import android.Manifest
import android.media.MediaScannerConnection
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import cc.arturia.yosei.R
import cc.arturia.yosei.data.SpConfig
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.event.FolderHideEvent
import cc.arturia.yosei.event.RxBus
import cc.arturia.yosei.module.base.BaseFragment
import cc.arturia.yosei.module.base.FragmentBackHelper
import cc.arturia.yosei.module.base.adapter.BaseSelectAdapter
import cc.arturia.yosei.module.video.presenter.VideoPresenter
import cc.arturia.yosei.module.video.presenter.VideoView
import cc.arturia.yosei.util.FileUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.tbruyelle.rxpermissions.RxPermissions
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_video_list.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import kotlin.properties.Delegates

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class VideoListFragment : BaseFragment(), VideoView, FragmentBackHelper.OnFragmentBackListener {

    private var realm: Realm by Delegates.notNull()
    private lateinit var presenter: VideoPresenter
    private lateinit var listAdapter: VideoListAdapter
    private lateinit var gridAdapter: VideoGridAdapter
    private var videos: List<Video>? = null

    private lateinit var loadingDialog: MaterialDialog

    override fun getLayoutResId(): Int = R.layout.fragment_video_list

    override fun initData() {
        realm = Realm.getDefaultInstance()
        videos = realm.where(Video::class.java)
                .equalTo("hide", false)
                .sort("name", Sort.ASCENDING)
                .findAll()
    }

    override fun initView() {
        if (SpConfig.getVideoListStyle(activity!!)) {
            iv_switch_layout.setImageResource(R.drawable.ic_grid)
        } else {
            iv_switch_layout.setImageResource(R.drawable.ic_list)
        }
        if (SpConfig.getFileHide(activity!!)) {
            iv_lock.visibility = View.VISIBLE
        } else {
            iv_lock.visibility = View.GONE
        }
        listAdapter = VideoListAdapter(activity!!, videos)
        gridAdapter = VideoGridAdapter(activity!!, videos)
        if (SpConfig.getVideoListStyle(activity!!)) {
            val layoutManager = LinearLayoutManager(activity!!)
            layoutManager.isItemPrefetchEnabled = false
            rv_video.layoutManager = layoutManager
            rv_video.adapter = listAdapter
        } else {
            rv_video.layoutManager = GridLayoutManager(activity, 2)
            rv_video.adapter = gridAdapter
        }

        loadingDialog = MaterialDialog.Builder(activity!!)
                .content(R.string.video_scanning)
                .progress(true, 0)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build()
    }

    override fun bindListener() {
        iv_scan.setOnClickListener { presenter.loadVideoList() }
        iv_switch_layout.setOnClickListener { switchLayout() }
        iv_lock.setOnClickListener { lockSelected() }
        iv_delete.setOnClickListener { deleteSelected() }
        iv_all_select.setOnClickListener {
            if (SpConfig.getVideoListStyle(activity!!)) {
                if (listAdapter.isAllSelect) {
                    listAdapter.clearAllSelect()
                } else {
                    listAdapter.setAllSelect()
                }
            } else {
                if (gridAdapter.isAllSelect) {
                    gridAdapter.clearAllSelect()
                } else {
                    gridAdapter.setAllSelect()
                }
            }
        }
        rv_video.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                if (SpConfig.getVideoListStyle(activity!!)) {
                    if (listAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        val video = listAdapter.data[position]
                        VideoActivity.launch(activity!!, video.path!!, video.name!!)
                    } else {
                        listAdapter.setChildSelect(position)
                    }
                } else {
                    if (gridAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        val video = gridAdapter.data[position]
                        VideoActivity.launch(activity!!, video.path!!, video.name!!)
                    } else {
                        gridAdapter.setChildSelect(position)
                    }
                }
            }

            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                if (SpConfig.getVideoListStyle(activity!!)) {
                    if (listAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        listAdapter.setMode(BaseSelectAdapter.MODE_SELECT)
                        fl_tool_bar.visibility = View.GONE
                        ll_file_manager.visibility = View.VISIBLE
                    }
                } else {
                    if (gridAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        gridAdapter.setMode(BaseSelectAdapter.MODE_SELECT)
                        fl_tool_bar.visibility = View.GONE
                        ll_file_manager.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VideoPresenter(this)

        if (videos == null || videos!!.isEmpty()) {
            val rxPermissions = RxPermissions(activity!!)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe { granted ->
                        if (granted!!) {
                            presenter.loadVideoList()
                        }
                    }
        }
    }

    override fun onBackPressed(): Boolean {
        if (listAdapter.getMode() == BaseSelectAdapter.MODE_SELECT) {
            clearListSelectMode()
            return true
        }
        if (gridAdapter.getMode() == BaseSelectAdapter.MODE_SELECT) {
            clearGridSelectMode()
            return true
        }
        return false
    }

    private fun clearGridSelectMode() {
        ll_file_manager.visibility = View.GONE
        fl_tool_bar.visibility = View.VISIBLE
        gridAdapter.clearAllSelect()
        gridAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
    }

    private fun clearListSelectMode() {
        ll_file_manager.visibility = View.GONE
        fl_tool_bar.visibility = View.VISIBLE
        listAdapter.clearAllSelect()
        listAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.close()
    }

    override fun subscribeEvents(): Subscription? {
        return RxBus.instance.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { o ->
                    if (o is FolderHideEvent) {
                        if (SpConfig.getFileHide(activity!!)) {
                            iv_lock.visibility = View.VISIBLE
                        } else {
                            iv_lock.visibility = View.GONE
                        }
                        clearListSelectMode()
                        clearGridSelectMode()
                        refreshVideoList()
                    }
                }
                .subscribe(RxBus.defaultSubscriber())
    }

    private fun switchLayout() {
        SpConfig.setVideoListStyle(activity!!, !SpConfig.getVideoListStyle(activity!!))
        if (SpConfig.getVideoListStyle(activity!!)) {
            val layoutManager = LinearLayoutManager(activity!!)
            layoutManager.isItemPrefetchEnabled = false
            rv_video.layoutManager = layoutManager
            rv_video.adapter = listAdapter
            iv_switch_layout.setImageResource(R.drawable.ic_grid)
        } else {
            rv_video.layoutManager = GridLayoutManager(activity, 2)
            rv_video.adapter = gridAdapter
            iv_switch_layout.setImageResource(R.drawable.ic_list)
        }
    }

    private fun lockSelected() {
        val select: BooleanArray = if (SpConfig.getVideoListStyle(activity!!)) {
            listAdapter.select
        } else {
            gridAdapter.select
        }
        if (select.isEmpty()) {
            return
        }
        for (i in select.indices.reversed()) {
            if (select[i]) {
                val video = videos!![i]
                val index = video.path!!.lastIndexOf("/") + 1
                val sb = StringBuilder(video.path)
                sb.insert(index, ".")
                FileUtil.rename(video.path, sb.toString())
                val newVideo = Video()
                newVideo.name = "." + video.name
                newVideo.path = sb.toString()
                newVideo.format = video.format
                newVideo.size = video.size
                newVideo.thumb = video.thumb
                newVideo.progress = video.progress
                newVideo.duration = video.duration
                newVideo.timestamp = video.timestamp
                newVideo.hide = true
                realm.executeTransaction { realm.copyToRealmOrUpdate(newVideo) }
                realm.executeTransaction { video.deleteFromRealm() }
            }
        }
        if (SpConfig.getVideoListStyle(activity!!)) {
            listAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        } else {
            gridAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        }
        ll_file_manager.visibility = View.GONE
        fl_tool_bar.visibility = View.VISIBLE
        refreshVideoList()
    }

    private fun deleteSelected() {
        val select: BooleanArray = if (SpConfig.getVideoListStyle(activity!!)) {
            listAdapter.select
        } else {
            gridAdapter.select
        }
        if (select.isEmpty()) {
            return
        }
        val paths = ArrayList<String>()
        for (i in select.indices.reversed()) {
            if (select[i]) {
                paths.add(videos!![i].path!!)
                FileUtil.deleteFileByPath(videos!![i].path)
            }
        }
        MediaScannerConnection.scanFile(activity, Array(paths.size) { i -> paths[i] }, null, null)
        val result: RealmResults<Video> = realm.where(Video::class.java)
                .`in`("path", Array(paths.size) { i -> paths[i] })
                .findAll()
        realm.executeTransaction { result.deleteAllFromRealm() }
        if (SpConfig.getVideoListStyle(activity!!)) {
            listAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        } else {
            gridAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        }
        ll_file_manager.visibility = View.GONE
        fl_tool_bar.visibility = View.VISIBLE
        refreshVideoList()
    }

    private fun refreshVideoList() {
        videos = realm.where(Video::class.java)
                .equalTo("hide", false)
                .sort("name", Sort.ASCENDING)
                .findAll()
        if (SpConfig.getVideoListStyle(activity!!)) {
            listAdapter.setNewData(videos)
        } else {
            gridAdapter.setNewData(videos)
        }
    }

    override fun onVideoListLoaded(videoList: List<Video>) {
        refreshVideoList()
    }

    override fun setPresenter(presenter: VideoPresenter) {
        this.presenter = presenter
    }

    override fun showLoading() {
        loadingDialog.show()
    }

    override fun hideLoading() {
        loadingDialog.dismiss()
    }

    override fun handleError(error: Throwable) {
        Log.e("VideoListFragment", "Error: " + error.message)
    }
}