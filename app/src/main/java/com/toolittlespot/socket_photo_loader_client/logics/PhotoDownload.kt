package com.toolittlespot.socket_photo_loader_client.logics

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class PhotoDownload(val id: Long, val root: View, val img: ImageView, val cover: ImageView, val refresh: ImageView, val percentage: TextView){
    var isDownloaded = false
}