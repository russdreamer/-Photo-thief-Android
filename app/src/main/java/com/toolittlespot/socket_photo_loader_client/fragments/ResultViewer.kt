package com.toolittlespot.socket_photo_loader_client.fragments


import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.synnapps.carouselview.CarouselView

import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.convertDpToPixels
import com.toolittlespot.socket_photo_loader_client.logics.showToast
import java.io.File

class ResultViewer : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var photoPathList: Array<String>
    private lateinit var carouselViews: ArrayList<ImageView>
    private lateinit var backGround: ImageView
    private lateinit var carousel: CarouselView
    private var prevPosition = 0
    private var isFullScreen = false

    fun passPhotos(pathList: Array<String>){
        this.photoPathList = pathList
        carouselViews = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =  inflater.inflate(R.layout.fragment_result_viewer, container, false)
        configViews()

        return fragmentView
    }

    private fun configViews() {
        configBgView()
        configCarousel()
    }

    private fun configCarousel() {
        carousel = fragmentView.findViewById(R.id.carousel_view)
        carousel.pageCount = photoPathList.size
        normalScreenView()

        carousel.setImageListener { position, imageView ->
            carouselViews.add(position, imageView)

            if (carouselViews.size == photoPathList.size){
                setNearbyViews(prevPosition)
            }
        }

        carousel.addOnPageChangeListener(getChangeListener())
    }

    private fun getChangeListener(): ViewPager.OnPageChangeListener? {
        return object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (isFullScreen)
                    carousel.pauseCarousel()

                setNearbyViews(position)
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }
        }

    }

    fun setNearbyViews(position: Int){
        if (carouselViews[position].drawable == null)
            setPageView(position, carouselViews[position])

        if (position >= prevPosition){
            if (prevPosition - 1 >= 0 && carouselViews[prevPosition - 1].drawable != null)
                carouselViews[prevPosition - 1].setImageDrawable(null)

            if (position + 1 < carouselViews.size)
                setPageView(position + 1, carouselViews[position + 1])
        }
        else {
            if (prevPosition + 1 < carouselViews.size && carouselViews[prevPosition + 1].drawable != null)
                carouselViews[prevPosition + 1].setImageDrawable(null)

            if (position - 1 >= 0)
                setPageView(position - 1, carouselViews[position - 1])

            else if (position != prevPosition - 1 && prevPosition == carouselViews.lastIndex){
                carouselViews[prevPosition - 1].setImageDrawable(null)
                carouselViews[prevPosition].setImageDrawable(null)
            }
        }

        prevPosition = position
    }

    private fun setPageView(position: Int, imageView: ImageView) {
            imageView.setImageDrawable(Drawable.createFromPath(photoPathList[position]))
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private fun fullScreenView(){
        isFullScreen = true
        backGround.visibility = View.VISIBLE
        carousel.setPadding(0, 0, 0, 0)
        carousel.pauseCarousel()

        carousel.setImageClickListener {
            normalScreenView()
        }
    }

    private fun normalScreenView(){
        isFullScreen = false
        backGround.visibility = View.INVISIBLE
        val padding = convertDpToPixels(10)
        carousel.setPadding(padding, padding, padding, padding)
        carousel.slideInterval = 3000
        carousel.playCarousel()

        carousel.setImageClickListener {
            fullScreenView()
        }
    }

    private fun configBgView() {
        backGround = fragmentView.findViewById(R.id.black_bg)
    }


}
