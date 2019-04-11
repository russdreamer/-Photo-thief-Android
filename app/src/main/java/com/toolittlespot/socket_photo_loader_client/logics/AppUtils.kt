package com.toolittlespot.socket_photo_loader_client.logics

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

fun showSnackBar(view: View, text: String){
    Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
        .setAction("Action", null).show()
}

fun showToast(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}