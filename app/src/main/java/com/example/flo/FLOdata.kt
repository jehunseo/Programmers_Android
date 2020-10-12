package com.example.flo

import android.media.MediaPlayer
import java.net.URL


class FLOdata{
    companion object {
        val urlJson =
            URL("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/song.json")

        var duration: Int = 0 //sec length data
        var fileURL: String = ""
        var lyrics = arrayListOf<String>()
        var mediaPlayer: MediaPlayer? = null
        var pausePosition: Int = 0 //ms time data
        var statePlaying: Boolean = false
        var timeline = arrayListOf<Int>()
        var timeindex = -1
    }
}