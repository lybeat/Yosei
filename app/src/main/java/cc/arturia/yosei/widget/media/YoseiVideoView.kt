package cc.arturia.yosei.widget.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import cc.arturia.yosei.R
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

/**
 * Author: Arturia
 * Date: 2018/11/22
 */
class YoseiVideoView : FrameLayout, IYoseiController {

    private var currentState = STATE_IDLE
    private var targetState = STATE_IDLE

    private lateinit var surfaceHolder: IRenderView.ISurfaceHolder
    private var mediaPlayer: IMediaPlayer? = null
    private var renderView: IRenderView? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    private var videoRotationDegree: Int = 0
    private var videoSarNum: Int = 0
    private var videoSarDen: Int = 0

    private var uri: Uri? = null

    private val allRenders = ArrayList<Int>()
    private var currentRenderIndex = 0
    private var currentRender = RENDER_SURFACE_VIEW

    private var currentBufferPercentage: Int = 0
    private var seekWhenPrepared: Int = 0

    private var onPreparedListener: IMediaPlayer.OnPreparedListener? = null
    private var onCompletionListener: IMediaPlayer.OnCompletionListener? = null
    private var onErrorListener: IMediaPlayer.OnErrorListener? = null
    private var onInfoListener: IMediaPlayer.OnInfoListener? = null
    private var onVideoSizeChangedListener: IMediaPlayer.OnVideoSizeChangedListener? = null
    private var onBufferingUpdateListener: IMediaPlayer.OnBufferingUpdateListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initVideoView()
    }

    private fun initVideoView() {
        initRenders()
        initBackgroundPlay()
    }

    private fun initRenders() {
        allRenders.clear()
        allRenders.add(RENDER_SURFACE_VIEW)
        allRenders.add(RENDER_TEXTURE_VIEW)
        currentRender = allRenders[currentRenderIndex]
        setRender(currentRender)
    }

    private fun initBackgroundPlay() {

    }

    private fun openVideo() {
        if (uri == null) {
            return
        }
        release()
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        try {
            mediaPlayer = createPlayer(0)
            mediaPlayer!!.setOnPreparedListener { onPrepared() }
            mediaPlayer!!.setOnCompletionListener { onCompletion() }
            mediaPlayer!!.setOnVideoSizeChangedListener { _, width, height, sar_num, sar_den ->
                onVideoSizeChanged(width, height, sar_num, sar_den)
            }
            mediaPlayer!!.setOnErrorListener { _, what, extra -> onError(what, extra) }
            mediaPlayer!!.setOnInfoListener { _, what, extra -> onInfo(what, extra) }
            mediaPlayer!!.setOnBufferingUpdateListener { _, percent -> onBufferingUpdate(percent) }
            mediaPlayer!!.dataSource = uri.toString()
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer!!.setScreenOnWhilePlaying(true)
            mediaPlayer!!.prepareAsync()

            currentState = STATE_PREPARING
        } catch (e: Exception) {
            Log.e(TAG, "Unable to open video: $uri", e)
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            onError(0, 0)
        }
    }

    private fun onPrepared() {
        currentState = STATE_PREPARED
        if (onPreparedListener != null) {
            onPreparedListener!!.onPrepared(mediaPlayer)
        }
        videoWidth = mediaPlayer!!.videoWidth
        videoHeight = mediaPlayer!!.videoHeight
        if (seekWhenPrepared != 0) {
            seekTo(seekWhenPrepared.toLong())
        }
        if (videoWidth != 0 && videoHeight != 0) {
            renderView!!.setVideoSize(videoWidth, videoHeight)
            renderView!!.setVideoSampleAspectRatio(videoSarNum, videoSarDen)
            if (!renderView!!.shouldWaitForResize() || (surfaceWidth != 0 && surfaceHeight != 0)) {
                if (targetState == STATE_PLAYING) {
                    start()
                }
            }
        } else {
            if (targetState == STATE_PLAYING) {
                start()
            }
        }
    }

    private fun onCompletion() {
        if (onCompletionListener != null) {
            onCompletionListener!!.onCompletion(mediaPlayer)
        }
        currentState = STATE_PLAYBACK_COMPLETED
        targetState = STATE_PLAYBACK_COMPLETED
    }

    private fun onVideoSizeChanged(width: Int, height: Int, sar_num: Int, sar_den: Int) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener!!.onVideoSizeChanged(mediaPlayer, width, height, sar_num, sar_den)
        }
        videoWidth = mediaPlayer!!.videoWidth
        videoHeight = mediaPlayer!!.videoHeight
        videoSarNum = mediaPlayer!!.videoSarNum
        videoSarDen = mediaPlayer!!.videoSarDen
        if (videoWidth != 0 && videoHeight != 0) {
            renderView!!.setVideoSize(videoWidth, videoHeight)
            renderView!!.setVideoSampleAspectRatio(videoSarNum, videoSarDen)

            requestLayout()
        }
    }

    private fun onError(what: Int, extra: Int): Boolean {
        if (windowToken != null) {
            val messageId: Int = if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                R.string.VideoView_error_text_invalid_progressive_playback
            } else {
                R.string.VideoView_error_text_unknown
            }

            AlertDialog.Builder(context)
                    .setMessage(messageId)
                    .setPositiveButton(R.string.VideoView_error_button
                    ) { _, _ ->
                        if (onErrorListener != null) {
                            onErrorListener!!.onError(mediaPlayer, what, extra)
                        }
                    }
                    .setCancelable(false)
                    .show()
        }
        return true
    }

    private fun onInfo(what: Int, extra: Int): Boolean {
        if (onInfoListener != null) {
            onInfoListener!!.onInfo(mediaPlayer, what, extra)
        }
        when (what) {
            IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:")
            IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:")
            IMediaPlayer.MEDIA_INFO_BUFFERING_START -> Log.d(TAG, "MEDIA_INFO_BUFFERING_START:")
            IMediaPlayer.MEDIA_INFO_BUFFERING_END -> Log.d(TAG, "MEDIA_INFO_BUFFERING_END:")
            IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: $extra")
            IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING -> Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:")
            IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:")
            IMediaPlayer.MEDIA_INFO_METADATA_UPDATE -> Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:")
            IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE -> Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:")
            IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT -> Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:")
            IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                videoRotationDegree = what
                Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: $what")
                renderView!!.setVideoRotation(extra)
            }
            IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START -> Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:")
        }

        return true
    }

    private fun onBufferingUpdate(percent: Int) {
        if (onBufferingUpdateListener != null) {
            onBufferingUpdateListener!!.onBufferingUpdate(mediaPlayer, percent)
        }
    }

    private val renderCallback = object : IRenderView.IRenderCallback {
        override fun onSurfaceCreated(holder: IRenderView.ISurfaceHolder, width: Int, height: Int) {
            surfaceWidth = width
            surfaceHeight = height
            val isValidState = targetState == STATE_PLAYING
            val hasValidSize = !renderView!!.shouldWaitForResize() || (videoWidth == width && videoHeight == height)
            if (isValidState && hasValidSize) {
                if (seekWhenPrepared != 0) {
                    seekTo(seekWhenPrepared.toLong())
                }
                start()
            }
        }

        override fun onSurfaceChanged(holder: IRenderView.ISurfaceHolder, format: Int, width: Int, height: Int) {
            surfaceHolder = holder
            surfaceWidth = width
            surfaceHeight = height
            holder.bindToMediaPlayer(mediaPlayer)
        }

        override fun onSurfaceDestroyed(holder: IRenderView.ISurfaceHolder) {
            mediaPlayer!!.setDisplay(null)
        }
    }

    private fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            currentState = STATE_IDLE
            targetState = STATE_IDLE
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocus(null)
        }
    }

    private fun isInPlayState() =
            currentState != STATE_ERROR &&
                    currentState != STATE_IDLE &&
                    currentState != STATE_PREPARING

    private fun createPlayer(playerType: Int): IMediaPlayer {
        return when (playerType) {
            1 -> AndroidMediaPlayer()
            2 -> IjkExoMediaPlayer(context)
            else -> configIjkPlayer()
        }
    }

    private fun configIjkPlayer(): IjkMediaPlayer {
        val ijkPlayer = IjkMediaPlayer()
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)

        return ijkPlayer
    }

    private fun setRenderView(renderView: IRenderView?) {
        this.renderView = renderView!!
//        this.renderView.setAspectRatio()
        if (videoWidth > 0 && videoHeight > 0) {
            this.renderView!!.setVideoSize(videoWidth, videoHeight)
        }
        if (videoSarNum > 0 && videoSarDen > 0) {
            this.renderView!!.setVideoSampleAspectRatio(videoSarNum, videoSarDen)
        }

        val renderUiView = this.renderView!!.view
        val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER)
        renderUiView.layoutParams = lp
        addView(renderUiView)

        this.renderView!!.addRenderCallback(renderCallback)
        this.renderView!!.setVideoRotation(videoRotationDegree)
    }

    private fun setRender(render: Int) {
        when (render) {
            RENDER_TEXTURE_VIEW -> {
                val renderView = TextureRenderView(context)
                renderView.surfaceHolder.bindToMediaPlayer(mediaPlayer)
                renderView.setVideoSize(mediaPlayer!!.videoWidth, mediaPlayer!!.videoHeight)
                renderView.setVideoSampleAspectRatio(mediaPlayer!!.videoSarNum, mediaPlayer!!.videoSarDen)
//                renderView.setAspectRatio()
                setRenderView(renderView)
            }
            RENDER_SURFACE_VIEW -> {
                val renderView = SurfaceRenderView(context)
                setRenderView(renderView)
            }
        }
    }

    fun setVideoPath(path: String) {
        setVideoUri(Uri.parse(path))
    }

    fun setVideoUri(uri: Uri) {
        this.uri = uri
        openVideo()
    }

    fun switchRender() {
        currentRenderIndex++
        currentRenderIndex %= allRenders.size
        currentRender = allRenders[currentRenderIndex]
        setRender(currentRender)
    }

    fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener) {
        onErrorListener = listener
    }

    override fun setMediaPlayer(player: IYoseiController) {

    }

    override fun start() {
        if (isInPlayState()) {
            mediaPlayer!!.start()
            currentState = STATE_PLAYING
        }
        targetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlayState()) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                currentState = STATE_PAUSED
            }
        }
        targetState = STATE_PAUSED
    }

    override fun resume() {
        mediaPlayer!!.start()
    }

    override fun stop() {
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        currentState = STATE_IDLE
        targetState = STATE_IDLE
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.abandonAudioFocus(null)
    }

    override fun getDuration(): Long {
        return if (isInPlayState()) {
            mediaPlayer!!.duration
        } else {
            0
        }
    }

    override fun getCurrentPosition(): Long {
        return if (isInPlayState()) {
            mediaPlayer!!.currentPosition
        } else {
            0
        }
    }

    override fun seekTo(pos: Long) {
        if (isInPlayState()) {
            mediaPlayer!!.seekTo(pos)
        }
    }

    override fun isPlaying(): Boolean {
        return if (isInPlayState()) {
            mediaPlayer!!.isPlaying
        } else {
            false
        }
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    companion object {
        private const val TAG = "YoseiVideoView"

        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5

        private const val RENDER_SURFACE_VIEW = 1
        private const val RENDER_TEXTURE_VIEW = 2
    }
}