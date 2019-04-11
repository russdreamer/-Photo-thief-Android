package com.toolittlespot.socket_photo_loader_client.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.toolittlespot.socket_photo_loader_client.R
import java.net.URLEncoder


class OnNumberGetter : Fragment() {
    lateinit var eventName: String

    fun passEventName(name: String){
        eventName = name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        println(URLEncoder.encode(eventName, "UTF-8"))
        return inflater.inflate(R.layout.fragment_on_number_getter, container, false)
    }


}
