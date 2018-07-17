package com.m3sv.droidupnp.presentation.main

import android.app.Activity
import android.content.Context
import android.provider.MediaStore
import io.reactivex.Single
import java.util.HashSet
import javax.inject.Inject


class GalleryRepository @Inject constructor(private val context: Context) {

    data class ImageInfo(val data: String, val title: String)

    data class VideoInfo(val data: String, val title: String)

    fun getAllImages(): Single<HashSet<ImageInfo>> {
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

    fun getAllVideos(): Single<HashSet<VideoInfo>> {
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