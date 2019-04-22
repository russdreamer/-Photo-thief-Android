package com.toolittlespot.socket_photo_loader_client.logics

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.toolittlespot.socket_photo_loader_client.BIG_PREVIEW
import com.toolittlespot.socket_photo_loader_client.ROOT_SITE
import com.toolittlespot.socket_photo_loader_client.SMALL_PREVIEW
import org.jsoup.Jsoup

class Photo (val id: Long, val imgSrc: String){
    companion object {
        fun gelAllPhotos(url: String): List<Photo>{
            val doc = Jsoup.connect(url).get()
            val items = doc.getElementsByClass("item_list")

            return items.map {
                val img = it.select("img")
                val src = img.attr("src")
                val id = getPhotoIdFromSrc(src).toLong()

                Photo(id, "$ROOT_SITE$src")
            }
        }

        fun getPhotoByID(id: Long): Drawable? {
            return getDrawableFromSrc("$ROOT_SITE$SMALL_PREVIEW$id.jpg")
        }

        fun getPreviewByID(id: Long): Drawable? {
            return getDrawableFromSrc("$ROOT_SITE$BIG_PREVIEW$id.jpg")
        }
    }
}