package com.toolittlespot.socket_photo_loader_client

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Window
import android.view.WindowManager
import com.toolittlespot.socket_photo_loader_client.fragments.GettingWaysMenu
import android.os.StrictMode
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        requestPermission(this)
    }

    private fun requestPermission(context: MainActivity) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val hasPermission = (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                context,
                Array(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                REQUEST_WRITE_STORAGE
            )
        } else {
            startApplication()
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0)
            super.onBackPressed()
    }

    private fun startApplication() {
        changeMainLayout(GettingWaysMenu())
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApplication()
                    Toast.makeText(this, PERMISSION_ACCEPT, Toast.LENGTH_LONG).show()
                } else {
                    finishAffinity()
                }
            }
        }
    }
}
