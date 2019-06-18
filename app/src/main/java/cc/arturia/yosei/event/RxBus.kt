package cc.arturia.yosei.event

import android.util.Log

import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject

/**
 * Author: Arturia
 * Date: 2017/6/8
 */
class RxBus {

    private val eventBus = PublishSubject.create<Any>()

    private object HolderClass {
        internal val INSTANCE = RxBus()
    }

    fun post(event: Any) {
        eventBus.onNext(event)
    }

    fun toObservable(): Observable<Any> {
        return eventBus
    }

    companion object {

        private const val TAG = "RxBus"

        val instance: RxBus
            get() = HolderClass.INSTANCE

        fun defaultSubscriber(): Subscriber<Any> {
            return object : Subscriber<Any>() {
                override fun onCompleted() {
                    Log.d(TAG, "RxBus onCompleted")
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "RxBus onError")
                }

                override fun onNext(o: Any) {
                    Log.d(TAG, "RxBus onNext")
                }
            }
        }
    }
}
