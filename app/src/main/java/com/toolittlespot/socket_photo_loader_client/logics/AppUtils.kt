package com.toolittlespot.socket_photo_loader_client.logics

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import java.util.regex.Pattern

fun showSnackBar(view: View, text: String){
    Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
        .setAction("Action", null).show()
}

fun showToast(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun getPhotoIdFromSrc(src: String): String{
    val regex = Pattern.compile("([0-9]+)\\.jpg")
    return getElementByRegex(src, regex, 1).first()

}

private fun getElementByRegex(text: String, regex: Pattern, group: Int): List<String> {
    val list = ArrayList<String>()
    val matcher = regex.matcher(text)
    while (matcher.find()) {
        if (matcher.group(group).trim().isNotEmpty())
            list.add(matcher.group(group))
    }
    return list
}