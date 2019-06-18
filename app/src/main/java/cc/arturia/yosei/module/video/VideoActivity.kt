package cc.arturia.yosei.module.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import cc.arturia.yosei.R
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.module.base.FullScreenActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_player.*
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.properties.Delegates

/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class VideoActivity : FullScreenActivity() {

    private var realm: Realm by Delegates.notNull()

    private var videoPath: String? = null
    private var videoUri: Uri? = null
    private var videoName: String? = null
    private var video: Video? = null

    private var backPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initPlayer()
        start()
    }

    override fun getLayoutResId(): Int = R.layout.activity_player

    override fun initData() {
        val intent = intent
        videoPath = intent.getStringExtra(VIDEO_PATH)
        videoName = intent.getStringExtra(VIDEO_NAME)
        val action = intent.action
        if (!TextUtils.isEmpty(action)) {
            if (action == Intent.ACTION_VIEW) {
                videoPath = intent.dataString
            } else if (action == Intent.ACTION_SEND) {
                videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        }
        realm = Realm.getDefaultInstance()
        video = realm.where(Video::class.java).equalTo("name", videoName).findFirst()
    }

    override fun initView() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        mc_player.setMediaPlayer(video_view)
        mc_player.setMediaName(videoName!!)
    }

    override fun bindListener() {
        mc_player.setNextListener(View.OnClickListener {
            // 播放下一集
        })
        mc_player.setGroupListener(View.OnClickListener {
            // 显示选集面板
        })
        mc_player.setBackListener(View.OnClickListener {
            backPressed = true
            finish()
        })
        video_view.setOnErrorListener(IMediaPlayer.OnErrorListener { _, _, _ ->
            backPressed = true
            finish()
            true
        })
    }

    private fun initPlayer() {
        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")
    }

    private fun start() {
        when {
            videoPath != null -> video_view!!.setVideoPath(videoPath!!)
            videoUri != null -> video_view!!.setVideoUri(videoUri!!)
            else -> {
                Log.e(TAG, "Null data source")
                finish()
            }
        }
        video_view!!.start()
        video_view!!.seekTo(video!!.progress!!)
    }

    override fun onBackPressed() {
        backPressed = true
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()

        if (backPressed) {
            updateVideo()
            video_view!!.stop()
        }
        IjkMediaPlayer.native_profileEnd()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun updateVideo() {
        realm.beginTransaction()
        video!!.progress = video_view!!.getCurrentPosition()
        realm.commitTransaction()
        realm.executeTransaction { realm.copyToRealmOrUpdate(video!!) }
    }

    companion object {

        private const val TAG = "VideoActivity"
        private const val VIDEO_PATH = "video_path"
        private const val VIDEO_NAME = "video_name"

        fun launch(context: Context, videoPath: String, videoName: String) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(VIDEO_PATH, videoPath)
            intent.putExtra(VIDEO_NAME, videoName)
            context.startActivity(intent)
        }
    }
}
