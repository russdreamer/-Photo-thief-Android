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
import android.widget.TextView
import com.toolittlespot.socket_photo_loader_client.MainActivity
import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.Photo.Companion.getPhotoByID
import com.toolittlespot.socket_photo_loader_client.logics.Photo.Companion.getPreviewByID
import com.toolittlespot.socket_photo_loader_client.logics.showToast
import kotlinx.coroutines.*
import java.io.IOException

class SinglePhoto : Fragment() {
    private lateinit var fragmentView: View
    private var photoID: Long = 0
    private lateinit var previewView: ImageView
    private lateinit var statusTxt: TextView
    private lateinit var tryAgainBtn: Button
    private lateinit var nextButton: Button
    private lateinit var photo: ImageView
    private var threadJob: Job? = null

    fun passParam(id: Long){
        this.photoID = id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = inflater.inflate(R.layout.fragment_single_photo, container, false)
        configViews()
        return fragmentView
    }

    private fun configViews() {
        configPreview()
        configLoadLabel()
        configTryAgainBtn()
        configNextBtn()
        configPhotoView()
    }

    private fun configPhotoView() {
        photo = fragmentView.findViewById(R.id.photo_view)
        loadPhoto()
    }

    private fun loadPhoto() {
        threadJob = GlobalScope.launch(Dispatchers.Main) {
            val success = withTimeoutOrNull(60_000L) {
                val task = GlobalScope.async(Dispatchers.Main) {
                    val op = getPhotoViews()
                    op
                }
                try {
                    task.await()
                } catch (e: Exception) {
                    when (e) {
                        is IOException, is TimeoutCancellationException -> setNetworkErrorMsg()
                        else -> setCriticalErrorMsg()
                    }
                    null
                }
            }

            if (success != null)
                if (success) {
                    statusTxt.visibility = View.GONE
                    nextButton.visibility = View.VISIBLE
                }

                else {
                    showToast(context!!, "Фотографии с таким ID не найдено!")
                    (activity as MainActivity).onBackPressed()
                }
            else setNetworkErrorMsg()
        }
    }

    private fun setNetworkErrorMsg() {
        tryAgainBtn.visibility = View.VISIBLE
        statusTxt.text = "Ошибка сети. Проверьте интернет-соединение."
    }

    private fun setCriticalErrorMsg() {
        statusTxt.text = "Критическая ошибка исполнения программы!"
    }

    private fun getPhotoViews(): Boolean {
        val drawable = getPhotoByID(photoID)
        return if (drawable != null){
            photo.setImageDrawable(drawable)
            configItemView(photo)
            true
        } else false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configItemView(photo: ImageView) {
        val prevDrawable = getPreviewByID(photoID)
        previewView.setImageDrawable(prevDrawable)

        photo.setOnClickListener {
            previewView.visibility = View.VISIBLE
        }

        photo.setOnLongClickListener {
            previewView.visibility = View.VISIBLE
            true
        }

        photo.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                if (previewView.visibility == View.VISIBLE){
                    previewView.visibility = View.GONE
                }
            true
        }
    }

    private fun configPreview() {
        previewView = fragmentView.findViewById(R.id.preview_image)
        previewView.visibility = View.GONE
    }

    private fun configTryAgainBtn() {
        tryAgainBtn = fragmentView.findViewById(R.id.try_again_btn)
        tryAgainBtn.setOnClickListener {
            tryAgainBtn.visibility = View.GONE
            statusTxt.text = "Загрузка..."
            loadPhoto()
        }
    }

    private fun configLoadLabel() {
        statusTxt = fragmentView.findViewById(R.id.status_txt)
    }

    private fun configNextBtn() {
        nextButton =  fragmentView.findViewById(R.id.next_btn)
        nextButton.setOnClickListener {
            val fragment = DownloadingGallery()
            val map = hashMapOf<Long, ImageView>()
            map[photoID] = photo
            fragment.passParams(map)
            (activity as MainActivity).changeMainLayout(fragment)
        }
        nextButton.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (threadJob != null)
            threadJob!!.cancel()
    }
}
