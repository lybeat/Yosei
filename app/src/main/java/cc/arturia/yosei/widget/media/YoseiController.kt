package cc.arturia.yosei.widget.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import cc.arturia.yosei.R
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.util.DateUtil
import cc.arturia.yosei.util.DateUtil.stringForTime
import cc.arturia.yosei.util.FormatUtil
import cc.arturia.yosei.util.ScreenUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.layout_brightness.view.*
import kotlinx.android.synthetic.main.layout_controller.view.*
import kotlinx.android.synthetic.main.layout_group.view.*
import kotlinx.android.synthetic.main.layout_media_controller.view.*
import kotlinx.android.synthetic.main.layout_media_info.view.*
import kotlinx.android.synthetic.main.layout_progress_text.view.*
import kotlinx.android.synthetic.main.layout_volume.view.*

/**
 * Author: Arturia
 * Date: 2018/11/21
 */
class YoseiController : FrameLayout, IYoseiController {

    private lateinit var player: IYoseiController

    private var showing: Boolean = false

    private var progress: Float = 0.toFloat()
    private var heapScrollY: Float = 0.toFloat()
    private var screenWidth: Int = 0
    private var adjustStatus = 0

    private lateinit var gestureDetector: GestureDetector

    private var nextListener: View.OnClickListener? = null
    private var prevListener: View.OnClickListener? = null
    private var groupListener: View.OnClickListener? = null
    private var backListener: View.OnClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.layout_controller, this)
        init()
    }

    private fun init() {
        initView()
        initGesture()
        bindListener()

        screenWidth = ScreenUtil.getScreenWidth(context)
    }

    private fun initView() {
        tv_phone_time.text = DateUtil.msToHM(System.currentTimeMillis())
    }

    private fun initGesture() {
        gestureDetector = GestureDetector(context, gestureListener)
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP ->
                    handleActionUp()
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun handleActionUp() {
        when (adjustStatus) {
            0 -> {
            }
            1 -> {
                val newPosition = (getCurrentPosition() + progress / screenWidth * 1000f * LEVEL_PROGRESS).toLong()
                if (newPosition < 0) {
                    seekTo(0)
                } else {
                    seekTo(newPosition)
                }
                start()
                ll_progress.visibility = View.GONE
            }
            2 -> ll_volume.visibility = View.GONE
            3 -> ll_brightness.visibility = View.GONE
        }
        adjustStatus = 0
        heapScrollY = 0f
        if (showing) {
            show()
        }
    }

    private fun bindListener() {
        iv_pause.setOnClickListener { onPlayPauseClick() }
        iv_prev.setOnClickListener(prevListener)
        iv_next.setOnClickListener(nextListener)
        iv_back.setOnClickListener(backListener)
        iv_subtitle.setOnClickListener { openSubtitleFile() }
        iv_group.setOnClickListener { showGroupLayout() }
        sb_player.setOnSeekBarChangeListener(seekBarListener)
    }

    private fun openSubtitleFile() {
        yosei_subtitle.visibility = View.VISIBLE
        yosei_subtitle.parseSubtitle("")
    }

    private fun showGroupLayout() {
        ll_group.visibility = View.VISIBLE
    }

    private fun onPlayPauseClick() {
        show()
        switchPlayStatus()
    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val duration = getDuration()
                val newPosition = duration * progress / 1000L
                seekTo(newPosition)
                Log.i("YoseiController", "newPosition: $newPosition")
                tv_time.text = String.format("%s/%s", stringForTime(newPosition), stringForTime(duration))
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            show(0)
            handler.removeMessages(MSG_SHOW_PROGRESS)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            show()
            setProgress()
            handler.sendEmptyMessage(MSG_SHOW_PROGRESS)
        }
    }

    fun setNextListener(listener: View.OnClickListener) {
        nextListener = listener
    }

    fun setPrevListener(listener: View.OnClickListener) {
        prevListener = listener
    }

    fun setGroupListener(listener: View.OnClickListener) {
        groupListener = listener
    }

    fun setBackListener(listener: View.OnClickListener) {
        backListener = listener
    }

    fun setMediaName(name: String) {
        tv_media_name.text = name
    }

    fun setVideoList(videos: List<Video>) {
        val adapter = GroupAdapter(context, videos)
        adapter.setCurrentVideoName(tv_media_name.text.toString())
        rv_group.layoutManager = LinearLayoutManager(context)
        rv_group.adapter = adapter
        rv_group.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                val video = adapter!!.data as Video
                Log.i("YoseiController", "video name: " + video.name)
            }
        })
    }

    private fun setProgress(): Long {
        val position = getCurrentPosition()
        val duration = getDuration()
        if (duration > 0) {
            val pos = 1000L * position / duration
            sb_player.progress = pos.toInt()
            sb_player.max = 1000
        }
        val percent = getBufferPercentage()
        sb_player.secondaryProgress = percent * 10

        if (tv_time != null)
            tv_time.text = String.format("%s/%s", stringForTime(position), stringForTime(duration))

        return position
    }

    private fun switchPlayStatus() {
        Log.i("YoseiController", "isPlaying: " + isPlaying())
        if (isPlaying()) {
            pause()
        } else {
            resume()
        }
    }

    private fun show(timeout: Int) {
        ll_media_controller.visibility = View.VISIBLE
        ll_media_info.visibility = View.VISIBLE
        tv_phone_time.text = DateUtil.msToHM(System.currentTimeMillis())

        handler.sendEmptyMessage(MSG_SHOW_PROGRESS)

        if (timeout != 0) {
            handler.removeMessages(MSG_FADE_OUT)
            handler.sendEmptyMessageDelayed(MSG_FADE_OUT, timeout.toLong())
        }

        showing = true
    }

    fun show() {
        show(DEFAULT_TIMEOUT)
    }

    fun hide() {
        ll_media_controller.visibility = View.GONE
        ll_media_info.visibility = View.GONE

        handler.removeMessages(MSG_SHOW_PROGRESS)

        showing = false
    }

    private fun toggle() {
        if (showing) {
            hide()
        } else {
            show()
        }
    }

    override fun setMediaPlayer(player: IYoseiController) {
        this.player = player
    }

    override fun start() {
        player.start()
    }

    override fun pause() {
        player.pause()
        iv_pause.setImageResource(R.drawable.ic_play)
    }

    override fun resume() {
        player.resume()
        iv_pause.setImageResource(R.drawable.ic_pause)
    }

    override fun stop() {
        player.stop()
    }

    override fun getDuration(): Long = player.getDuration()

    override fun getCurrentPosition(): Long = player.getCurrentPosition()

    override fun seekTo(pos: Long) {
        player.seekTo(pos)
    }

    override fun isPlaying(): Boolean = player.isPlaying()

    override fun getBufferPercentage(): Int = player.getBufferPercentage()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> show()
//            MotionEvent.ACTION_UP -> show()
//            MotionEvent.ACTION_CANCEL -> hide()
//        }

        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val uniqueDown = event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                switchPlayStatus()
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !isPlaying()) {
                start()
                iv_pause.setImageResource(R.drawable.ic_pause)
            }
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && isPlaying()) {
                pause()
                iv_pause.setImageResource(R.drawable.ic_play)
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return super.dispatchKeyEvent(event)
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide()
            }
            return super.dispatchKeyEvent(event)
        }

        return super.dispatchKeyEvent(event)
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val scrollX = e2.rawX - e1.rawX
            val scrollY = e2.rawY - e1.rawY

            when (adjustStatus) {
                // 进入调整状态
                0 ->
                    // 水平滑动，调整播放进度
                    if (Math.abs(scrollX) > Math.abs(scrollY)) {
                        adjustStatus = 1
                        adjustProgress(scrollX)
                    } else {
                        if (e1.rawX > screenWidth / 2 + INVALID_AREA) {
                            adjustStatus = 2
                            adjustVolume(distanceY)
                        } else if (e1.rawY < screenWidth / 2 - INVALID_AREA) {
                            adjustStatus = 3
                            adjustBrightness(distanceY)
                        }
                    }
                // 调整进度
                1 -> adjustProgress(scrollX)
                // 调整音量
                2 -> adjustVolume(distanceY)
                // 调整亮度
                3 -> adjustBrightness(distanceY)
            }

            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            toggle()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            switchPlayStatus()
            return true
        }
    }

    private fun adjustProgress(scrollX: Float) {
        pause()
        ll_progress.visibility = View.VISIBLE

        progress = scrollX
        val adjustSecond = (scrollX / screenWidth * LEVEL_PROGRESS).toInt()
        if (adjustSecond >= 0) {
            if (getCurrentPosition() / 1000 + adjustSecond > getDuration() / 1000) {
                tv_progress.text = String.format("%s/%s", DateUtil.msToString(getDuration()), DateUtil.msToString(getDuration()))
                tv_second.text = String.format("+%s", FormatUtil.format2Int(
                        (getDuration() - getCurrentPosition()) / 1000))
            } else {
                tv_progress.text = String.format("%s/%s", DateUtil.msToString((getCurrentPosition() + adjustSecond * 1000)),
                        DateUtil.msToString(getDuration()))
                tv_second.text = String.format("+%s", FormatUtil.format2Int(adjustSecond.toLong()))
            }
        } else {
            if (getCurrentPosition() / 1000 + adjustSecond < 0) {
                tv_progress.text = String.format("%s/%s", "00:00", DateUtil.msToString(getDuration()))
                tv_second.text = String.format("-%s", FormatUtil.format2Int(getCurrentPosition() / 1000))
            } else {
                tv_progress.text = String.format("%s/%s", DateUtil.msToString((getCurrentPosition() + adjustSecond * 1000)),
                        DateUtil.msToString(getDuration()))
                tv_second.text = String.format("-%s", FormatUtil.format2Int(Math.abs(adjustSecond).toLong()))
            }
        }
    }

    private fun adjustVolume(scrollY: Float) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ll_volume.visibility = View.VISIBLE
        if (scrollY > LEVEL_VOLUME) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
        } else if (scrollY < -LEVEL_VOLUME) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
        }
        val sound = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        pb_volume.progress = sound
    }

    private fun adjustBrightness(scrollY: Float) {
        Log.i("@@@#@", "scrollY: $scrollY")
        heapScrollY += scrollY
        val brightness = ScreenUtil.getScreenBrightness(context as Activity)
        ll_brightness.visibility = View.VISIBLE
        if (heapScrollY > LEVEL_BRIGHTNESS) {
            heapScrollY = 0f
            if (brightness >= 0.9f) {
                ScreenUtil.setScreenBrightness(context as Activity, 1.0f)
            } else {
                ScreenUtil.setScreenBrightness(context as Activity,
                        ScreenUtil.getScreenBrightness(context as Activity) + 0.1f)
            }
        } else if (heapScrollY < -LEVEL_BRIGHTNESS) {
            heapScrollY = 0f
            if (brightness <= 0.1f) {
                ScreenUtil.setScreenBrightness(context as Activity, 0.0f)
            } else {
                ScreenUtil.setScreenBrightness(context as Activity,
                        ScreenUtil.getScreenBrightness(context as Activity) - 0.1f)
            }
        }
        pb_brightness.progress = (ScreenUtil.getScreenBrightness(context as Activity) * 100).toInt()
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_FADE_OUT -> hide()
                MSG_SHOW_PROGRESS -> {
                    if (isPlaying()) {
                        setProgress()
                        sendEmptyMessageDelayed(MSG_SHOW_PROGRESS, 100)
                    }
                }
            }
        }
    }

    companion object {
        private const val LEVEL_PROGRESS = 100
        private const val LEVEL_VOLUME = 2
        private const val LEVEL_BRIGHTNESS = 20
        private const val INVALID_AREA = 150

        private const val MSG_FADE_OUT = 0
        private const val MSG_SHOW_PROGRESS = 1

        private const val DEFAULT_TIMEOUT = 3000
    }
}