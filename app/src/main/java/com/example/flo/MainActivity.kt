package com.example.flo

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread

/*
Used Library
OKHttp 4.9 : https://square.github.io/okhttp/
Glide 4.11.0 : https://github.com/bumptech/glide
Gson 2.9.0
 */

class MainActivity : AppCompatActivity() {
    val client = OkHttpClient()
    val request = Request.Builder().url(FLOdata.urlJson).build()
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get mediaplayer running time and Implement on seekBar
        val runnable = object : Runnable {
            override fun run() {
                val intent = Intent(this@MainActivity, FullLyricsActivity::class.java)
                if (FLOdata.mediaPlayer != null) {
                    thread(start = true) {
                        FLOdata.timeindex = -1
                        val runningtime = FLOdata.mediaPlayer?.currentPosition
                        if (runningtime != null) {
                            if (runningtime >= FLOdata.duration * 1000) {
                                FLOdata.statePlaying = false
                                runOnUiThread { imageButton2.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24) }
                                FLOdata.mediaPlayer?.release()
                            }
                            for (i in 0..(FLOdata.lyrics.size - 2)) {
                                if (runningtime > FLOdata.timeline[i] && runningtime < FLOdata.timeline[i + 1]) {
                                    FLOdata.timeindex = i
                                    runOnUiThread { textLyrics.text = FLOdata.lyrics[i] }
                                    break
                                }
                            }
                            if (runningtime > FLOdata.timeline[FLOdata.lyrics.size-1]) {
                                FLOdata.timeindex = FLOdata.lyrics.size - 1
                                runOnUiThread { textLyrics.text = FLOdata.lyrics[FLOdata.lyrics.size-1] }
                            }
                            seekBar.progress = (runningtime / 1000)
                            intent.putExtra("progress", seekBar.progress)
                        }

                    }
                }
                handler.postDelayed(this, 100)
            }
        }

        //take Json Data and Implement on UI
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", "Failure!")
            }

            override fun onResponse(call: Call, response: Response) {
                val strJson = response?.body?.string()
                val gson = Gson()
                val FLOData: FLOJson = gson.fromJson(strJson, FLOJson::class.java)
                val image = Glide.with(this@MainActivity).load(FLOData.image)

                FLOdata.duration = FLOData.duration
                FLOdata.fileURL = FLOData.file

                parseLyrics(FLOData.lyrics)

                runOnUiThread {
                    if (FLOData != null) {
                        textTitle.text = FLOData.title
                        textArtist.text = FLOData.singer
                        textAlbum.text = FLOData.album
                        textDuration.text =
                            "${(FLOdata.duration / 60)}:${(FLOdata.duration % 60)}"
                        image.into(imageAlbumArt)
                        seekBar.max = FLOdata.duration
                    }
                }
            }
        }) //parse all data

        //make audio pause / play
        imageButton2.setOnClickListener() {
            when (FLOdata.statePlaying) {
                false -> {
                    handler.post(runnable)
                    FLOdata.statePlaying = true
                    imageButton2.setBackgroundResource(R.drawable.ic_baseline_pause_24)
                    thread(start = true) {
                        FLOdata.mediaPlayer = MediaPlayer()?.apply {
                            setAudioStreamType(AudioManager.STREAM_MUSIC)
                            setDataSource(FLOdata.fileURL)
                            prepare() // might take long! (for buffering, etc)
                        }
                        FLOdata.mediaPlayer?.seekTo(FLOdata.pausePosition)
                        FLOdata.mediaPlayer?.start()
                    }


                }
                true -> {
                    handler.removeCallbacks(runnable)
                    FLOdata.statePlaying = false
                    imageButton2.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                    FLOdata.mediaPlayer?.pause()
                    FLOdata.pausePosition = seekBar.progress * 1000
                }

            }
        }

        //change playing time with seekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var tempSeekbar = FLOdata.pausePosition
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar!!.progress = progress
                tempSeekbar = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar!!.progress = tempSeekbar
                FLOdata.pausePosition = tempSeekbar * 1000
                if (tempSeekbar > seekBar.max) FLOdata.pausePosition = seekBar.max
                FLOdata.mediaPlayer?.seekTo(FLOdata.pausePosition)
                handler.post(runnable)
            }

        })

        //if click lyrics text, move to FullLyricsActivity
        textLyrics.setOnClickListener() {
            val intent = Intent(this@MainActivity, FullLyricsActivity::class.java)
            startActivity(intent)
        }
        //parse Lyrics to time and lyrics line
    }
    fun parseLyrics(string: String): Unit {
        //[00:16:200]we wish you a merry christmas\n[00:18:300]we wish you a merry christmas\n
        for (s in string.split('\n')) {
            FLOdata.timeline.add(
                s.substring(1..2).toInt() * 60000 + s.substring(4..5)
                    .toInt() * 1000 + s.substring(7..7) .toInt() * 100
            )
            FLOdata.lyrics.add(s.substring(11, s.length))
        }
    }
}




/*
{
  "singer": "챔버오케스트라",
  "album": "캐롤 모음",
  "title": "We Wish You A Merry Christmas",
  "duration": 198,
  "image": "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/cover.jpg",
  "file": "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/music.mp3",
  "lyrics": "[00:16:200]we wish you a merry christmas\n[00:18:300]we wish you a merry christmas\n[00:21:100]we wish you a merry christmas\n[00:23:600]and a happy new year\n[00:26:300]we wish you a merry christmas\n[00:28:700]we wish you a merry christmas\n[00:31:400]we wish you a merry christmas\n[00:33:600]and a happy new year\n[00:36:500]good tidings we bring\n[00:38:900]to you and your kin\n[00:41:500]good tidings for christmas\n[00:44:200]and a happy new year\n[00:46:600]Oh, bring us some figgy pudding\n[00:49:300]Oh, bring us some figgy pudding\n[00:52:200]Oh, bring us some figgy pudding\n[00:54:500]And bring it right here\n[00:57:000]Good tidings we bring \n[00:59:700]to you and your kin\n[01:02:100]Good tidings for Christmas \n[01:04:800]and a happy new year\n[01:07:400]we wish you a merry christmas\n[01:10:000]we wish you a merry christmas\n[01:12:500]we wish you a merry christmas\n[01:15:000]and a happy new year\n[01:17:700]We won't go until we get some\n[01:20:200]We won't go until we get some\n[01:22:800]We won't go until we get some\n[01:25:300]So bring some out here\n[01:29:800]연주\n[02:11:900]Good tidings we bring \n[02:14:000]to you and your kin\n[02:16:500]good tidings for christmas\n[02:19:400]and a happy new year\n[02:22:000]we wish you a merry christmas\n[02:24:400]we wish you a merry christmas\n[02:27:000]we wish you a merry christmas\n[02:29:600]and a happy new year\n[02:32:200]Good tidings we bring \n[02:34:500]to you and your kin\n[02:37:200]Good tidings for Christmas \n[02:40:000]and a happy new year\n[02:42:400]Oh, bring us some figgy pudding\n[02:45:000]Oh, bring us some figgy pudding\n[02:47:600]Oh, bring us some figgy pudding\n[02:50:200]And bring it right here\n[02:52:600]we wish you a merry christmas\n[02:55:300]we wish you a merry christmas\n[02:57:900]we wish you a merry christmas\n[03:00:500]and a happy new year"
}
 */