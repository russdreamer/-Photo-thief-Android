package com.toolittlespot.socket_photo_loader_client.logics

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
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

fun getDrawableFromSrc(imgSrc: String): Drawable? {
    val stream = URL(imgSrc).content as InputStream
    stream.use {
        return Drawable.createFromStream(stream, "img")
    }
}

fun convertDpToPixels(dp: Int): Int{
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun getBitmapFromSrc(src: String): Bitmap{
    return BitmapFactory.decodeStream(URL(src).openConnection().getInputStream())
}

fun saveImgToAppFolder(bitmap: Bitmap, name: String, context: Context): String{
    val dir = getApplicationFolder("preview", context)
    dir.mkdirs()
    val file = dir.resolve(name)

    val out = FileOutputStream(file)
    out.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    return file.absolutePath
}

fun clearPreviewFolder(context: Context){
    val dir = getApplicationFolder("preview", context)
    if (dir.exists())
        dir.delete()
}

fun getAlbumStorageDir(): File {
    val file = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ),"Photo Getter")

    if (!file.mkdirs()) {

    }
    return file
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

private fun getApplicationFolder(folderName: String, context: Context?): File {
    val cw = ContextWrapper(context)
    return cw.getDir(folderName, Context.MODE_PRIVATE)

}