package com.toolittlespot.socket_photo_loader_client.fragments


import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.toolittlespot.socket_photo_loader_client.MainActivity
import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.PhotoDownload
import com.toolittlespot.socket_photo_loader_client.logics.clearPreviewFolder
import com.toolittlespot.socket_photo_loader_client.logics.convertDpToPixels
import com.toolittlespot.socket_photo_loader_client.logics.getBitmapFromSrc
import kotlinx.coroutines.*

class DownloadingGallery : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var doneBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var gallery: LinearLayout
    private lateinit var chosenPhotos: HashMap<Long, ImageView>
    private lateinit var photoViews: ArrayList<PhotoDownload>
    private lateinit var resPhotoPath: ArrayList<String>
    private lateinit var threadJobs: ArrayList<Job>

    fun passParams(photos: HashMap<Long, ImageView>){
        this.chosenPhotos = photos
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_uploading_gallery, container, false)
        clearPreviewFolder(context!!)
        photoViews = arrayListOf()
        resPhotoPath = arrayListOf()
        threadJobs = arrayListOf()
        configViews()
        return fragmentView
    }

    private fun configViews() {
        configDoneBtn()
        configCancelBtn()
        configGallery()
    }

    private fun configCancelBtn() {
        cancelBtn = fragmentView.findViewById(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            stopThreadJob()
        }
    }

    private fun configDoneBtn() {
        doneBtn = fragmentView.findViewById(R.id.done_btn)
        doneBtn.visibility = View.GONE
        doneBtn.setOnClickListener {
            while (activity!!.supportFragmentManager.backStackEntryCount > 1){
                (activity as MainActivity).onBackPressed()
            }
            val fragment = ResultViewer()
            fragment.passPhotos(resPhotoPath.toTypedArray())
            (activity as MainActivity).changeMainLayout(fragment)
        }
    }

    private fun configGallery() {
        gallery = fragmentView.findViewById(R.id.galery_view)
        addAllPreviews(chosenPhotos)
        chosenPhotos.clear()
        startDownload()
    }

    private fun startDownload() {
        GlobalScope.launch(Dispatchers.Main){
            photoViews.forEach {
                GlobalScope.launch(Dispatchers.Main){
                    downloadPhoto(it)
                }
            }
        }
    }

    private suspend fun downloadPhoto(photo: PhotoDownload) {
        val imgPath = withTimeoutOrNull(60_000L) {
            val task = async {
                downloadAndSave(photo)
            }
            try {
                threadJobs.add(task)
                task.await()
            }
            catch (e: Exception) {
                setErrorImgState(photo)
                null
            }
        }
        if (imgPath != null){
            resPhotoPath.add(imgPath)
            setDoneImgState(photo)
            updateUI()
        }
        else setErrorImgState(photo)
    }

    private fun setErrorImgState(photo: PhotoDownload) {
        photo.percentage.text = "0%"
        photo.percentage.visibility = View.INVISIBLE
        photo.cover.visibility = View.VISIBLE
        photo.refresh.visibility = View.VISIBLE
    }

    private fun setBeginImgState(photo: PhotoDownload) {
        photo.percentage.text = "0%"
        photo.percentage.visibility = View.VISIBLE
        photo.cover.visibility = View.VISIBLE
        photo.refresh.visibility = View.INVISIBLE
    }

    private fun setDoneImgState(photo: PhotoDownload) {
        photo.percentage.visibility = View.INVISIBLE
        photo.cover.visibility = View.INVISIBLE
        photo.refresh.visibility = View.INVISIBLE
        photo.isDownloaded = true
    }

    private fun saveToGallery(bitmap: Bitmap, fileName: String): String? {
        return MediaStore.Images.Media.insertImage(context!!.contentResolver, bitmap, fileName , null)
    }

    private suspend fun downloadAndSave(photo: PhotoDownload): String? {
        val delay = Math.random()+ 1
        var perc = 0
        repeat(20) {
            perc += 5
            delay(delay.toLong() * 1000)
            photo.percentage.text = "$perc%"
        }
        photo.isDownloaded = true

        synchronized(this){
            val bitmap = getBitmapFromSrc("https://marathon-photo.ru/static2/preview3/stock-photo-8064-8485-4385364.jpg")
            return saveToGallery(bitmap, "${photo.id}.jpg")
        }
    }

    private fun addAllPreviews(previewMap: Map<Long, ImageView>) {
        var row: LinearLayout? = null
        var isRowComplete = true

        for (photo in previewMap){
            val photoView = createPhotoView(photo)
            configPhotoView(photoView)
            photoViews.add(photoView)

            if (isRowComplete) {
                row = LinearLayout(context)
                row.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                row.weightSum = 2F
                row.orientation = LinearLayout.HORIZONTAL
                row.addView(photoView.root)

                gallery.addView(row)
                isRowComplete = false
            } else {
                row!!.addView(photoView.root)
                isRowComplete = true
            }
        }
    }

    private fun configPhotoView(photoView: PhotoDownload) {
        val width = convertDpToPixels(100)
        photoView.root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, width, 1F)
        photoView.refresh.visibility = View.INVISIBLE
        photoView.percentage.text = "0%"
        photoView.refresh.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                setBeginImgState(photoView)
                downloadPhoto(photoView) }
        }
    }

    private fun createPhotoView(photo: Map.Entry<Long, ImageView>): PhotoDownload {
        val root = layoutInflater.inflate(R.layout.upload_photo_item, null)
        val refresh = root.findViewById<ImageView>(R.id.refresh)
        val img = root.findViewById<ImageView>(R.id.photo_view)
        val cover = root.findViewById<ImageView>(R.id.photo_cover)
        val percentage = root.findViewById<TextView>(R.id.percentage_txt)
        img.setImageDrawable(photo.value.drawable)

        return PhotoDownload(photo.key, root, img, cover, refresh, percentage)
    }

    private fun updateUI(){
        if (isDownloadDone()){
            cancelBtn.visibility = View.GONE
            doneBtn.visibility = View.VISIBLE
        }
    }

    private fun isDownloadDone(): Boolean{
        photoViews.forEach {
            if (!it.isDownloaded)
                return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopThreadJob()
        chosenPhotos.clear()
        photoViews.clear()
    }

    private fun stopThreadJob() {
        threadJobs.forEach {
            it.cancel()
        }
        threadJobs.clear()
    }
}
