package com.toolittlespot.socket_photo_loader_client.logics

import org.jsoup.Jsoup

class Event(val name: String, val date: String){
    companion object {
        fun getAllEvents(): List<Event>{
            val eventsURL = "https://marathon-photo.ru/members/categories.php"
            var doc = Jsoup.connect(eventsURL).get()
            val pastComp = doc.getElementsByClass("past_competitions_list")
            val table = pastComp.select("table")
            val events = table.select("tr")

            return events.map {
                val date = it.child(0).text()
                val name = it.child(1).text()
                Event(name, date)
            }
        }
    }
}