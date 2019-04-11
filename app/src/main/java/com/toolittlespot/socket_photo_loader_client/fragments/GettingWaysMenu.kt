package com.toolittlespot.socket_photo_loader_client.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.toolittlespot.socket_photo_loader_client.MainActivity
import com.toolittlespot.socket_photo_loader_client.R


class GettingWaysMenu : Fragment() {
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =  inflater.inflate(R.layout.fragment_getting_ways_menu, container, false)
        fragmentView.findViewById<Button>(R.id.on_number_button).setOnClickListener {
            (activity as MainActivity).changeMainLayout(Events())
        }
        fragmentView.findViewById<Button>(R.id.on_id_button).setOnClickListener{
            (activity as MainActivity).changeMainLayout(OnIdGetter())
        }

        return fragmentView;
    }


}
