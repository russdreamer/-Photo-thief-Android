package com.toolittlespot.socket_photo_loader_client.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.toolittlespot.socket_photo_loader_client.R

class Photos : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var eventName: String
    private lateinit var number: String

    fun passParams(eventName: String, runNumber: String){
        this.eventName = eventName
        this.number = runNumber
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =  inflater.inflate(R.layout.fragment_photos, container, false)

        return fragmentView
    }


}
