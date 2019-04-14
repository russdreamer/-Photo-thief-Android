package com.toolittlespot.socket_photo_loader_client.fragments


import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.Event
import com.toolittlespot.socket_photo_loader_client.logics.Photo
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.net.URL

class Photos : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var eventName: String
    private lateinit var number: String
    private lateinit var gallery: LinearLayout
    private lateinit var preview_view: ImageView
    private lateinit var statusTxt: TextView
    private lateinit var tryAgainBtn: Button
    private var threadJob: Job? = null

    fun passParams(eventName: String, runNumber: String){
        this.eventName = eventName
        this.number = runNumber
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =  inflater.inflate(R.layout.fragment_photos, container, false)
        configViews()
        return fragmentView
    }

    private fun configViews() {
        configPreview()
        configLoadLabel()
        configTryAgainBtn()
        configGrid()
    }

    private fun configTryAgainBtn() {
        tryAgainBtn = fragmentView.findViewById(R.id.try_again_btn)
        tryAgainBtn.setOnClickListener {
            tryAgainBtn.visibility = View.GONE
            statusTxt.text = "Загрузка..."
            fillGrid()
        }
    }

    private fun configLoadLabel() {
        statusTxt = fragmentView.findViewById(R.id.status_txt)
    }

    private fun configPreview() {
        preview_view = fragmentView.findViewById(R.id.preview_image)
        preview_view.visibility = View.GONE
    }

    private fun configGrid() {
        gallery = fragmentView.findViewById(R.id.galery_view)
        fillGrid()
    }

    private fun fillGrid(){
        threadJob = GlobalScope.launch(Dispatchers.Main){
            val photoViewList = withTimeoutOrNull(60_000L) {
                val task = GlobalScope.async {
                    getAllPhotoViews()
                }
                try {
                    task.await()
                }
                catch (e: Exception) {
                    when (e) {
                        is IOException, is TimeoutCancellationException -> setNetworkErrorMsg()
                        else -> setCriticalErrorMsg()
                    }
                    null
                }
            }

            if (! photoViewList.isNullOrEmpty()){
                addPhotosToGrid(photoViewList)
                statusTxt.visibility = View.GONE
            }

        }
    }

    private fun addPhotosToGrid(list: List<ImageView>) {
        var row: LinearLayout? = null
        var isRowComplete = true

        for (photo in list){
            if (isRowComplete) {
                row = LinearLayout(context)
                row.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                row.weightSum = 2F
                row.orientation = LinearLayout.HORIZONTAL
                row.setPadding(0, 50, 0, 0)
                row.addView(photo)

                gallery.addView(row)
                isRowComplete = false
            } else {
                row!!.addView(photo)
                isRowComplete = true
            }
        }
    }

    private fun getAllPhotoViews(): List<ImageView> {
        var hasNextPage = true
        val allPagePhotos = arrayListOf<Photo>()

        var i = 1
        while (hasNextPage){
            val link = Event.generateGalleryLink(eventName, number, i++)
            val photos = Photo.gelAllPhotos(link)

            if (photos.isNotEmpty())
                allPagePhotos.addAll(photos)
            else hasNextPage = false
        }

        val photoViewList = arrayListOf<ImageView>()

        for (i in 0..allPagePhotos.lastIndex) {
            val photo = getImageFromSrc(allPagePhotos[i].imgSrc)
            configPhotoView(photo, allPagePhotos[i].imgSrc)
            photoViewList.add(photo)
        }
        return photoViewList
    }

    private fun setCriticalErrorMsg() {
        statusTxt.text = "Критическая ошибка исполнения программы!"
    }

    private fun setNetworkErrorMsg() {
        tryAgainBtn.visibility = View.VISIBLE
        statusTxt.text = "Ошибка сети. Проверьте интернет-соединение."
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configPhotoView(photo: ImageView, photoSrc: String) {
        photo.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 400, 1F)
        photo.scaleType = ImageView.ScaleType.FIT_CENTER

        val prevLink = photoSrc.replace(Regex("preview([0-9])"), "preview3")
        val preview = getImageFromSrc(prevLink)

        photo.setOnClickListener {

        }

        photo.setOnLongClickListener {
            preview_view.setImageDrawable(preview.drawable)
            preview_view.visibility = View.VISIBLE
            true
        }

        photo.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                if (preview_view.visibility == View.VISIBLE){
                    preview_view.visibility = View.GONE
                    preview_view.setImageDrawable(null)
                }
            true
        }
    }

    private fun getImageFromSrc(imgSrc: String): ImageView {
        val img = ImageView(context)
        val stream = URL(imgSrc).content as InputStream
        stream.use {
            val drawable = Drawable.createFromStream(stream, "img")
            img.setImageDrawable(drawable)
        }
        return img
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (threadJob != null)
            threadJob!!.cancel()
    }
}


