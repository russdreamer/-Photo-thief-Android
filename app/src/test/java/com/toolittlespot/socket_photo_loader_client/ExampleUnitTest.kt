package com.toolittlespot.socket_photo_loader_client

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val str = "/static/preview1/someImg.jpg"
        val newStr = str.replace(Regex("preview([0-9])"), "preview2")
        println(str)
        println(newStr)
        assertEquals(4, 2 + 2)
    }
}
