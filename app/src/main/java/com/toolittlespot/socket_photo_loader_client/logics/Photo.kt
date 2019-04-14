package com.toolittlespot.socket_photo_loader_client.logics

import org.jsoup.Jsoup

class Photo (val id: Long, val imgSrc: String){
    companion object {
        fun gelAllPhotos(url: String): List<Photo>{
            val root = "https://marathon-photo.ru"
            val doc = Jsoup.connect(url).get()
            val items = doc.getElementsByClass("item_list")

            return items.map {
                val img = it.select("img")
                val src = img.attr("src")
                val id = getPhotoIdFromSrc(src).toLong()

                Photo(id, "$root$src")
            }
        }
    }
}