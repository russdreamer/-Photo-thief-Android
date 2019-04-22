package com.toolittlespot.socket_photo_loader_client.logics

import com.toolittlespot.socket_photo_loader_client.EVENTS
import com.toolittlespot.socket_photo_loader_client.PAST_COMPETITION
import com.toolittlespot.socket_photo_loader_client.ROOT_SITE
import org.jsoup.Jsoup
import java.net.URLEncoder

class Event(val name: String, val date: String){
    companion object {
        fun getAllEvents(): List<Event>{
            val eventsURL = "$ROOT_SITE$EVENTS"
            val doc = Jsoup.connect(eventsURL).get()
            val pastComp = doc.getElementsByClass(PAST_COMPETITION)
            val table = pastComp.select("table")
            val events = table.select("tr")

            return events.map {
                val date = it.child(0).text()
                val name = it.child(1).text()
                Event(name, date)
            }
        }

        fun generateGalleryLink(eventName: String, number: String, page: Int): String{
            val rootURL = "$ROOT_SITE/members/ajax.php?page="
            val competition = "&competition="
            val person = "&sphoto=on&search="
            val tailLink = "&partly=0&search_by_title=0"
            val eventNameURL = URLEncoder.encode(eventName, "UTF-8")

            return "$rootURL$page$competition$eventNameURL$person$number$tailLink"
        }
    }
}