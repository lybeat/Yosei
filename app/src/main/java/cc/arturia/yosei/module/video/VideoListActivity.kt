package cc.arturia.yosei.module.video

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import cc.arturia.yosei.R
import cc.arturia.yosei.data.SpConfig
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.module.base.ImmersiveActivity
import cc.arturia.yosei.module.base.adapter.BaseSelectAdapter
import cc.arturia.yosei.util.FileUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_video_list.*
import java.io.File
import kotlin.properties.Delegates

/**
 * Author: Arturia
 * Date: 2018/11/15
 */
class VideoListActivity : ImmersiveActivity() {

    private var realm: Realm by Delegates.notNull()
    private var videos: List<Video>? = null
    private lateinit var listAdapter: VideoListAdapter
    private lateinit var gridAdapter: VideoGridAdapter

    private var folderName = ""

    override fun getLayoutResId(): Int = R.layout.activity_video_list

    override fun initData() {
        realm = Realm.getDefaultInstance()
        folderName = intent.getStringExtra("key_folder_name")
        if (folderName == getString(R.string.app_name)) {
            videos = realm.where(Video::class.java)
                    .equalTo("hide", true)
                    .sort("name", Sort.ASCENDING)
                    .findAll()
        } else {
            val files = File("").listFiles()
            if (files != null && files.isNotEmpty()) {
                val videoList = ArrayList<Video>()
                val mmr = MediaMetadataRetriever()
                for (file in files) {
                    val video = Video()
                    video.name = file.name
                    video.path = file.path
                    video.format = file.name.split(".")[1]
                    video.size = file.length()
                    video.thumb = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    video.progress = 0
                    video.duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                    video.timestamp = System.currentTimeMillis()
                    video.hide = false
                    videoList.add(video)
                }
                videos = videoList
            }
        }
    }

    override fun initView() {
        if (SpConfig.getVideoListStyle(this)) {
            iv_switch_layout.setImageResource(R.drawable.ic_grid)
        } else {
            iv_switch_layout.setImageResource(R.drawable.ic_list)
        }
        listAdapter = VideoListAdapter(this, videos)
        gridAdapter = VideoGridAdapter(this, videos)
        if (SpConfig.getVideoListStyle(this)) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.isItemPrefetchEnabled = false
            rv_video.layoutManager = layoutManager
            rv_video.adapter = listAdapter
        } else {
            rv_video.layoutManager = GridLayoutManager(this, 2)
            rv_video.adapter = gridAdapter
        }
    }

    override fun bindListener() {
        iv_switch_layout.setOnClickListener { switchLayout() }
        iv_unlock.setOnClickListener { unlockSelected() }
        iv_delete.setOnClickListener { deleteSelected() }
        iv_all_select.setOnClickListener {
            if (SpConfig.getVideoListStyle(this)) {
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
                if (SpConfig.getVideoListStyle(this@VideoListActivity)) {
                    if (listAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        val video = listAdapter.data[position]
                        VideoActivity.launch(this@VideoListActivity, video.path!!, video.name!!)
                    } else {
                        listAdapter.setChildSelect(position)
                    }
                } else {
                    if (gridAdapter.getMode() == BaseSelectAdapter.MODE_NORMAL) {
                        val video = gridAdapter.data[position]
                        VideoActivity.launch(this@VideoListActivity, video.path!!, video.name!!)
                    } else {
                        gridAdapter.setChildSelect(position)
                    }
                }
            }

            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                if (SpConfig.getVideoListStyle(this@VideoListActivity)) {
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

    private fun switchLayout() {
        SpConfig.setVideoListStyle(this, !SpConfig.getVideoListStyle(this))
        if (SpConfig.getVideoListStyle(this)) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.isItemPrefetchEnabled = false
            rv_video.layoutManager = layoutManager
            rv_video.adapter = listAdapter
            iv_switch_layout.setImageResource(R.drawable.ic_grid)
        } else {
            rv_video.layoutManager = GridLayoutManager(this, 2)
            rv_video.adapter = gridAdapter
            iv_switch_layout.setImageResource(R.drawable.ic_list)
        }
    }

    private fun unlockSelected() {
        val select: BooleanArray = if (SpConfig.getVideoListStyle(this)) {
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
                sb.delete(index, index + 1)
                FileUtil.rename(video.path, sb.toString())
                val newVideo = Video()
                newVideo.name = video.name!!.removePrefix(".")
                newVideo.path = sb.toString()
                newVideo.format = video.format
                newVideo.size = video.size
                newVideo.thumb = video.thumb
                newVideo.progress = video.progress
                newVideo.duration = video.duration
                newVideo.timestamp = video.timestamp
                newVideo.hide = false
                realm.executeTransaction { realm.copyToRealmOrUpdate(newVideo) }
                realm.executeTransaction { video.deleteFromRealm() }
            }
        }
        if (SpConfig.getVideoListStyle(this)) {
            listAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        } else {
            gridAdapter.setMode(BaseSelectAdapter.MODE_NORMAL)
        }
        ll_file_manager.visibility = View.GONE
        fl_tool_bar.visibility = View.VISIBLE
        refreshVideoList()
    }

    private fun deleteSelected() {
        val select: BooleanArray = if (SpConfig.getVideoListStyle(this)) {
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
        MediaScannerConnection.scanFile(this, Array(paths.size) { i -> paths[i] }, null, null)
        val result: RealmResults<Video> = realm.where(Video::class.java)
                .`in`("path", Array(paths.size) { i -> paths[i] })
                .findAll()
        realm.executeTransaction { result.deleteAllFromRealm() }
        if (SpConfig.getVideoListStyle(this)) {
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
                .equalTo("hide", true)
                .sort("name", Sort.ASCENDING)
                .findAll()
        if (SpConfig.getVideoListStyle(this)) {
            listAdapter.setNewData(videos)
        } else {
            gridAdapter.setNewData(videos)
        }
    }

    companion object {
        fun launch(context: Context, name: String) {
            val intent = Intent()
            intent.setClass(context, VideoListActivity::class.java)
            intent.putExtra("key_folder_name", name)
            context.startActivity(intent)
        }
    }
}