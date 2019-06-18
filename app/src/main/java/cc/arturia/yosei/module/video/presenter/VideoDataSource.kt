package cc.arturia.yosei.module.video.presenter

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import cc.arturia.yosei.app.Yosei
import cc.arturia.yosei.app.YoseiConfig
import cc.arturia.yosei.data.Video
import cc.arturia.yosei.util.FileUtil
import cc.arturia.yosei.util.PictureUtil
import io.realm.Realm
import io.realm.kotlin.delete
import rx.Observable
import java.io.File
import java.io.FilenameFilter
import kotlin.text.Typography.times


/**
 * Author: Arturia
 * Date: 2018/10/16
 */
class VideoDataSource : VideoContract {

    private object Holder {
        val instance = VideoDataSource()
    }

    companion object {
        fun get(): VideoDataSource = Holder.instance
    }

    override fun loadVideoList(): Observable<List<Video>> = Observable.create { subscriber ->
        val videos = ArrayList<Video>()
        val cr = Yosei.instance.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID)
        val cursor = MediaStore.Video.query(cr, uri, projection)
        if (cursor != null && cursor.moveToFirst()) {
            val realm = Realm.getDefaultInstance()
            do {
                val path = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.DATA))
                val name = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.DISPLAY_NAME))
                val format = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.MIME_TYPE))
                val duration = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Video.Media.DURATION))
                val size = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Video.Media.SIZE))
//                val timestamp = cursor.getLong(cursor.getColumnIndex(
//                        MediaStore.Video.Media.DATE_ADDED))
                val id = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Video.Media._ID))

                val thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        cr, id, MediaStore.Video.Thumbnails.MINI_KIND, BitmapFactory.Options())

                val video = realm.where(Video::class.java).equalTo("name", name).findFirst()
                if (video == null) {
                    val thumb = File(YoseiConfig.THUMB, "$name.jpg")
                    PictureUtil.savePictureToFile(Yosei.instance, thumbnail, thumb)
                    val realVideo = Video()
                    realVideo.name = name
                    realVideo.path = path
                    realVideo.format = format
                    realVideo.size = size
                    realVideo.thumb = thumb.path
                    realVideo.progress = 0L
                    realVideo.duration = duration
//                    realVideo.timestamp = timestamp
                    realVideo.hide = name.startsWith(".")
                    videos.add(realVideo)
                    realm.executeTransaction { realm.copyToRealmOrUpdate(realVideo) }
                } else {
                    if (FileUtil.isExist(video.path)) {
                        realm.beginTransaction()
                        video.hide = name.startsWith(".")
                        realm.commitTransaction()
                        videos.add(video)
                    } else {
                        realm.executeTransaction { video.deleteFromRealm() }
                    }
                }

                Log.i("@@@#", "video name: $name")
                Log.i("@@@#", "video format: $format")
                Log.i("@@@#", "video duration: $duration")
                Log.i("@@@#", "video size: $size")
                Log.i("@@@#", "video path: $path")
                Log.i("@@@#", "video hide: " + name.startsWith("."))
            } while (cursor.moveToNext())
            cursor.close()
            realm.close()

//            val time = System.currentTimeMillis()
//            Log.i("@@@#@", "start: $time")
//            subscriber.onNext(scanVideoFile(Environment.getExternalStorageDirectory().absolutePath + "/udian"))
//            Log.i("@@@#@", "end: " + (System.currentTimeMillis() - time))
            subscriber.onNext(videos)
            subscriber.onCompleted()
        }
        }

        private fun scanVideoFile(path: String): List<Video> {
            val videos = ArrayList<Video>()
            val file = File(path)
            Log.i("@@@#@", "isFile: " + file.isFile)
            for (f in file.listFiles()) {
                if (file.isFile) {
                    val files = file.listFiles { dir, name ->
                        !name.startsWith(".") && isVideo(name)
                    }
                    videos.addAll(getVideoList(files))
                } else if (file.isDirectory) {
                    videos.addAll(scanVideoFile(f.path))
                }
            }
            return videos
        }

        private fun getVideoList(files: Array<File>): List<Video> {
            val videos = ArrayList<Video>()
            for (file in files) {
                videos.add(createVideo(file))
            }
            return videos
        }

        private fun createVideo(file: File): Video {
            val mmr = MediaMetadataRetriever()
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
            return video
        }

        private fun isVideo(name: String): Boolean {
            return name.endsWith(".mp4")
                    || name.endsWith(".avi")
                    || name.endsWith(".wmv")
                    || name.endsWith(".mkv")
                    || name.endsWith(".flv")
                    || name.endsWith(".rmvb")
        }
    }