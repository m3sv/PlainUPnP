package com.m3sv.plainupnp.presentation.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.provider.MediaStore
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.presentation.main.data.ContentType
import com.m3sv.plainupnp.presentation.main.data.Item
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@ApplicationScope
class GalleryRepository @Inject constructor(private val context: Context) {

    private val _images: MutableLiveData<Set<Item>> = MutableLiveData()

    private val _videos: MutableLiveData<Set<Item>> = MutableLiveData()

    private val _all: MutableLiveData<Set<Item>> = MutableLiveData()

    fun getAll(): LiveData<Set<Item>> {
        Single.zip(
            getAllImages(),
            getAllVideos(),
            BiFunction<MutableSet<Item>, MutableSet<Item>, Set<Item>> { t1, t2 ->
                t1.addAll(t2)
                t1
            })
            .subscribeBy(onSuccess = _all::postValue, onError = Timber::e)
        return _all
    }

    fun getImages(): LiveData<Set<Item>> {
        getAllImages()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = _images::postValue, onError = Timber::e)
        return _images
    }

    fun getVideos(): LiveData<Set<Item>> {
        getAllVideos()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = _videos::postValue, onError = Timber::e)
        return _videos
    }

    private fun getAllImages(): Single<HashSet<Item>> {
        return Single.create {
            val imagesHashSet = HashSet<Item>()
            val projection =
                arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.TITLE)
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            with(cursor) {
                try {
                    moveToFirst()
                    do {
                        val data =
                            getString(getColumnIndexOrThrow(android.provider.MediaStore.Images.ImageColumns.DATA))
                        val title =
                            getString(getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.TITLE))
                        imagesHashSet.add(Item(data, title, ContentType.IMAGE))
                    } while (cursor.moveToNext())
                    close()
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            if (!it.isDisposed)
                it.onSuccess(imagesHashSet)
        }
    }

    private fun getAllVideos(): Single<HashSet<Item>> {
        return Single.create {
            val videoItemHashSet = HashSet<Item>()
            val projection =
                arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.TITLE)
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            with(cursor) {
                try {
                    moveToFirst()
                    do {
                        val data =
                            getString(getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA))
                        val title = getString(getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                        videoItemHashSet.add(Item(data, title, ContentType.VIDEO))
                    } while (cursor.moveToNext())
                    close()
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            if (!it.isDisposed)
                it.onSuccess(videoItemHashSet)
        }
    }
}