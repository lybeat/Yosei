package cc.arturia.yosei.widget.media

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import cc.arturia.yosei.util.FileUtil
import cc.arturia.yosei.widget.subtitle.FormatSRT
import cc.arturia.yosei.widget.subtitle.TimedTextFileFormat
import cc.arturia.yosei.widget.subtitle.TimedTextObject
import java.io.File
import java.io.FileInputStream

/**
 * Author: Arturia
 * Date: 2018/12/3
 */
class YoseiSubtitle : FrameLayout {

    private lateinit var mediaPlayer: IYoseiController
    private var ttff: TimedTextFileFormat? = null
    private var tto: TimedTextObject? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    fun setMediaPlayer(player: IYoseiController) {
        mediaPlayer = player
    }

    fun parseSubtitle(path: String) {
        val format = FileUtil.getExtensionName(path).toLowerCase()
        val file = File(path)
        val input = FileInputStream(file)
        when (format) {
            "srt" -> {
                ttff = FormatSRT()
                tto = ttff!!.parseFile(file.name, input)
                for (line in tto!!.toSRT()) {
                    Log.i("@@@#@", "srt: $line")
                }
                handler.sendEmptyMessageDelayed(MSG_SHOW_SUBTITLE, 100)
            }
            "ass" -> {
            }
            "stl" -> {
            }
            "scc" -> {
            }
            "xml" -> {
            }
        }
    }

    fun start() {
        visibility = View.VISIBLE
    }

    fun pause() {

    }

    fun resume() {

    }

    fun stop() {
        visibility = View.GONE
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SHOW_SUBTITLE -> {
                    if (mediaPlayer.isPlaying()) {
                        // 刷新字幕

                        sendEmptyMessageDelayed(MSG_SHOW_SUBTITLE, TIME_CYCLE)
                    }
                }
            }
        }
    }

    companion object {
        private const val MSG_SHOW_SUBTITLE = 10
        private const val TIME_CYCLE = 100L  // ms
    }
}