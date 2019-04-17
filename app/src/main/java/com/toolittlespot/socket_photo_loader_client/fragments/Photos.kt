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
import com.toolittlespot.socket_photo_loader_client.MainActivity
import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.net.URL

class Photos : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var eventName: String
    private lateinit var number: String
    private lateinit var gallery: LinearLayout
    private lateinit var previewView: ImageView
    private lateinit var statusTxt: TextView
    private lateinit var tryAgainBtn: Button
    private lateinit var clearAllBtn: Button
    private lateinit var selectAllBtn: Button
    private lateinit var selectedPhotos: HashMap<Long, ImageView>
    private lateinit var photoItemList: ArrayList<View>
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
        selectedPhotos = hashMapOf()
        photoItemList = arrayListOf()
        configViews()
        return fragmentView
    }

    private fun configViews() {
        configPreview()
        configLoadLabel()
        configTryAgainBtn()
        configSelectAllBtn()
        configClearBtn()
        configNextBtn()
        configGrid()
    }

    private fun configNextBtn() {
        fragmentView.findViewById<Button>(R.id.next_btn).setOnClickListener {
            if (selectedPhotos.isEmpty())
                showToast(context!!, "Не выбрана ни одна фотография!")
            else{
                val fragment = DownloadingGallery()
                fragment.passParams(selectedPhotos)
                (activity as MainActivity).changeMainLayout(fragment)
            }

        }
    }

    private fun configClearBtn() {
        clearAllBtn = fragmentView.findViewById(R.id.clear_all_btn)
        clearAllBtn.setOnClickListener {
            selectedPhotos.clear()
            photoItemList.forEach { item->
                val img = item.findViewById<ImageView>(R.id.photo_view)
                img.scaleX = 1F
                img.scaleY = 1F
            }
        }
    }

    private fun configSelectAllBtn() {
        selectAllBtn = fragmentView.findViewById(R.id.select_all_btn)
        selectAllBtn.setOnClickListener {
            selectedPhotos.clear()
            photoItemList.forEach { item->
                item.callOnClick()
            }
        }
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
        previewView = fragmentView.findViewById(R.id.preview_image)
        previewView.visibility = View.GONE
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
                photoItemList.addAll(photoViewList)
                statusTxt.visibility = View.GONE
            }
            else{
                if(photoViewList == null) {
                    setNetworkErrorMsg()
                }
                else {
                    showToast(context!!, "Фотографий с участником под таким номером не найдено!")
                    (activity as MainActivity).onBackPressed()
                }
            }

        }
    }

    private fun addPhotosToGrid(list: List<View>) {
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
                row.addView(photo)

                gallery.addView(row)
                isRowComplete = false
            } else {
                row!!.addView(photo)
                isRowComplete = true
            }
        }
    }

    private fun getAllPhotoViews(): List<View> {
        var hasNextPage = true
        val allPagePhotos = arrayListOf<Photo>()

        var page = 1
        while (hasNextPage){
            val link = Event.generateGalleryLink(eventName, number, page++)
            val photos = Photo.gelAllPhotos(link)

            if (photos.isNotEmpty())
                allPagePhotos.addAll(photos)
            else hasNextPage = false
        }

        val photoViewList = arrayListOf<View>()

        for (i in 0..allPagePhotos.lastIndex) {
            val photo = getDrawableFromSrc(allPagePhotos[i].imgSrc)

            if (photo != null){
                val item = getPhotoItem(photo)
                configItemView(item, allPagePhotos[i])
                photoViewList.add(item)
            }
        }
        return photoViewList
    }

    private fun getPhotoItem(photo: Drawable): View {
        val layout = layoutInflater.inflate(R.layout.photo_item, null)
        layout.findViewById<ImageView>(R.id.photo_view).setImageDrawable(photo)
        return layout
    }

    private fun setCriticalErrorMsg() {
        statusTxt.text = "Критическая ошибка исполнения программы!"
    }

    private fun setNetworkErrorMsg() {
        tryAgainBtn.visibility = View.VISIBLE
        statusTxt.text = "Ошибка сети. Проверьте интернет-соединение."
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configItemView(imgLayout: View, photo: Photo) {
        val width = convertDpToPixels(100)
        imgLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, width, 1F)

        val prevLink = photo.imgSrc.replace(Regex("preview([0-9])"), "preview3")
        val prevPath = getPreviewPath(prevLink, photo)
        val imgView = imgLayout.findViewById<ImageView>(R.id.photo_view)

        imgLayout.setOnClickListener {
            if (selectedPhotos.remove(photo.id) != null){
                imgView.scaleX = 1F
                imgView.scaleY = 1F
            }
            else {
                selectedPhotos[photo.id] = imgView
                imgView.scaleX = 0.9F
                imgView.scaleY = 0.9F
            }
        }

        imgLayout.setOnLongClickListener {
            previewView.setImageDrawable(Drawable.createFromPath(prevPath))
            previewView.visibility = View.VISIBLE
            true
        }

        imgLayout.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                if (previewView.visibility == View.VISIBLE){
                    previewView.visibility = View.GONE
                    previewView.setImageDrawable(null)
                }
            true
        }
    }

    private fun getPreviewPath(prevLink: String, photo: Photo): String? {
        synchronized(this){

            val bitmap = getBitmapFromSrc(prevLink)
            return saveImgToAppFolder(bitmap, photo.id.toString(), context!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (threadJob != null)
            threadJob!!.cancel()

        photoItemList.clear()
    }
}


