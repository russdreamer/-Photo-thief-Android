package com.toolittlespot.socket_photo_loader_client.fragments


import android.os.Bundle
import kotlinx.coroutines.*
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.toolittlespot.socket_photo_loader_client.MainActivity

import com.toolittlespot.socket_photo_loader_client.R
import com.toolittlespot.socket_photo_loader_client.logics.Event
import java.io.IOException
import java.lang.Exception


class Events : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var statusTxt: TextView
    private lateinit var tryAgainBtn: Button
    private lateinit var eventList: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = inflater.inflate(R.layout.fragment_events, container, false)
        configLoadLabel()
        configTryAgainBtn()
        configGrid()

        return fragmentView
    }

    private fun configTryAgainBtn() {
        tryAgainBtn = fragmentView.findViewById(R.id.try_again_btn)
        tryAgainBtn.setOnClickListener {
            tryAgainBtn.visibility = View.GONE
            statusTxt.text = "Загрузка..."
            getGridContent()
        }
    }

    private fun configLoadLabel() {
        statusTxt = fragmentView.findViewById(R.id.status_txt)
    }

    private fun configGrid() {
        eventList = fragmentView.findViewById(R.id.event_list)
        getGridContent()
    }

    private fun getGridContent() {
        val job = GlobalScope.launch{

            val events = withTimeoutOrNull(10_000L){
                try {
                    Event.getAllEvents()
                }
                catch (e: IOException){
                    activity!!.runOnUiThread {
                        setNetworkErrorMsg()
                    }
                    null
                }
                catch (e: Exception){
                    activity!!.runOnUiThread {
                        setParsingErrorMsg()
                    }
                    null
                }
            }

            if (! events.isNullOrEmpty()){
                activity!!.runOnUiThread{
                    fillEvents(events)
                    statusTxt.visibility = View.GONE
                }
            }
        }
    }

    private fun setParsingErrorMsg() {
        statusTxt.text = "Критическая ошибка исполнения программы!"
    }

    private fun setNetworkErrorMsg() {
        tryAgainBtn.visibility = View.VISIBLE
        statusTxt.text = "Ошибка сети. Проверьте интернет-соединение."
    }

    private fun fillEvents(events: List<Event>) {
        events.forEach { event ->
            val child = layoutInflater.inflate(R.layout.event_item, null)
            child.findViewById<TextView>(R.id.event_date_txt).text = event.date
            child.findViewById<TextView>(R.id.event_name_txt).text = event.name
            child.setOnClickListener {
                val fragment = OnNumberGetter()
                fragment.passEventName(event.name)
                (activity as MainActivity).changeMainLayout(fragment)
            }
            eventList.addView(child)
        }
    }


}
