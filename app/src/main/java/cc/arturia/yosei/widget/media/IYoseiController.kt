package cc.arturia.yosei.widget.media

/**
 * Author: Arturia
 * Date: 2018/11/21
 */
interface IYoseiController {

    fun setEnabled(boolean: Boolean)

    fun setMediaPlayer(player: IYoseiController)

    fun start()

    fun pause()

    fun resume()

    fun stop()

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun seekTo(pos: Long)

    fun isPlaying(): Boolean

    fun getBufferPercentage(): Int
}