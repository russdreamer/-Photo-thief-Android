package com.toolittlespot.socket_photo_loader_client.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.toolittlespot.socket_photo_loader_client.MainActivity

import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.showToast


class OnNumberGetter : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var eventName: String
    private lateinit var numberTxt: EditText

    fun passEventName(name: String){
        eventName = name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = inflater.inflate(R.layout.fragment_on_number_getter, container, false)
        configViews()


        return fragmentView
    }

    private fun configViews() {
        fragmentView.findViewById<TextView>(R.id.event_name_txt).text = eventName
        numberTxt = fragmentView.findViewById(R.id.id_txt)
        configOkBtn()
    }

    private fun configOkBtn() {
        fragmentView.findViewById<Button>(R.id.ok_btn).setOnClickListener {
            if (numberTxt.text.isEmpty())
                showToast(context!!, "Введите стартовый номер!")
            else {
                val fragment = Photos()
                fragment.passParams(eventName, numberTxt.text.toString())
                (activity as MainActivity).changeMainLayout(fragment)
            }
        }
    }
}
