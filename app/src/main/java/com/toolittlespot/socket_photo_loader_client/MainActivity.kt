package com.toolittlespot.socket_photo_loader_client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Window
import android.view.WindowManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
    }

    private fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun changeMainLayout(newLayout: Fragment, addToBackStack: Boolean = true) {
        val fragmentManager = supportFragmentManager

        val transaction = fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(
                R.id.mainFragment,
                newLayout
            )
        if (addToBackStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }
}
