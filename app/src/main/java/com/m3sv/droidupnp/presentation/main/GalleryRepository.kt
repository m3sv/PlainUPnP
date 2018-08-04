package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.provider.MediaStore
import com.m3sv.droidupnp.di.scope.ApplicationScope
import com.m3sv.droidupnp.presentation.main.data.ImageInfo
import com.m3sv.droidupnp.presentation.main.data.VideoInfo
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


@ApplicationScope
class GalleryRepository @Inject constructor(private val context: Context) {

    private val _images: MutableLiveData<HashSet<ImageInfo>> = MutableLiveData()

    fun getImages(): LiveData<HashSet<ImageInfo>> {
        getAllImages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = _images::postValue, onError = Timber::e)
        return _images
    }

    private fun getAllImages(): Single<HashSet<ImageInfo>> {
        return Single.create {
            val imagesHashSet = HashSet<ImageInfo>()
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
                        imagesHashSet.add(ImageInfo(data, title))
                    } while (cursor.moveToNext())
                    close()
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            it.onSuccess(imagesHashSet)
        }
    }

    private fun getAllVideos(): Single<HashSet<VideoInfo>> {
        return Single.create {
            val videoItemHashSet = HashSet<VideoInfo>()
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
                        videoItemHashSet.add(VideoInfo(data, title))
                    } while (cursor.moveToNext())
                    close()
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            it.onSuccess(videoItemHashSet)
        }
    }
}