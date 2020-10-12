package com.example.flo

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_full_lyrics2.*
import kotlinx.android.synthetic.main.activity_main.imageButton2
import kotlin.concurrent.thread


class FullLyricsActivity : AppCompatActivity() {
    val mAdapter = MyAdapter(this, FLOdata.lyrics)
    val handler = Handler()
    var toggle = true
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_lyrics2)
        seekBar2.max = FLOdata.duration

        if(FLOdata.statePlaying) imageButton2.setBackgroundResource(R.drawable.ic_baseline_pause_24)
        else imageButton2.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)

        val runnable = object : Runnable {
            override fun run() {
                if (FLOdata.mediaPlayer != null) {
                    thread(start = true) {
                        val runningtime = FLOdata.mediaPlayer?.currentPosition
                        runOnUiThread{ mAdapter.notifyDataSetChanged() }
                        if (runningtime != null) {
                            if (runningtime >= FLOdata.duration * 1000) {
                                FLOdata.statePlaying = false
                                runOnUiThread { imageButton2.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24) }
                                FLOdata.mediaPlayer?.release()
                            }
                            seekBar2.progress = (runningtime / 1000)
                        }

                    }
                }
                handler.postDelayed(this, 100)
            }
        }
        handler.post(runnable)

        ListLyrics.adapter = mAdapter
        ListLyrics.setOnItemClickListener(OnItemClickListener { _, _, position, _ ->
            if(!toggle){
                FLOdata.pausePosition = FLOdata.timeline.get(position)
                seekBar2.progress = FLOdata.pausePosition / 1000
                FLOdata.mediaPlayer?.seekTo(FLOdata.pausePosition)
            }
            else
            {
                handler.removeCallbacks(runnable)
                finish()
            }

        })

        imageButton2.setOnClickListener(){
            when (FLOdata.statePlaying) {
                false -> {
                    handler.post(runnable)
                    FLOdata.statePlaying = true
                    imageButton2.setBackgroundResource(R.drawable.ic_baseline_pause_24)
                    thread(start = true) {
                        if (FLOdata.mediaPlayer == null) {
                            FLOdata.mediaPlayer = MediaPlayer()?.apply {
                                setAudioStreamType(AudioManager.STREAM_MUSIC)
                                setDataSource(FLOdata.fileURL)
                                prepare() // might take long! (for buffering, etc)
                            }
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
                    FLOdata.pausePosition = seekBar2.progress * 1000
                }

            }
        }

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var tempSeekbar = FLOdata.pausePosition
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar2!!.progress = progress
                tempSeekbar = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar2!!.progress = tempSeekbar
                FLOdata.pausePosition = tempSeekbar * 1000
                if (tempSeekbar > seekBar2.max) FLOdata.pausePosition = seekBar2.max
                FLOdata.mediaPlayer?.seekTo(FLOdata.pausePosition)
                handler.post(runnable)
            }

        })

        imageButton3.setOnClickListener(){
            handler.removeCallbacks(runnable)
            finish()
        }

        switch1.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                false -> {
                    toggle = true
                }
                true -> {
                    toggle = false
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        handler.removeCallbacksAndMessages(null)
    }
}
/*
전체 가사가 띄워진 화면이 있으며, 특정 가사 부분으로 이동할 수 있는 토글 버튼이 존재합니다.
토글 버튼 on: 특정 가사 터치 시 해당 구간부터 재생
토글 버튼 off: 특정 가사 터치 시 전체 가사 화면 닫기
전체 가사 화면 닫기 버튼이 있습니다.
현재 재생 중인 부분의 가사가 하이라이팅 됩니다.

 */
