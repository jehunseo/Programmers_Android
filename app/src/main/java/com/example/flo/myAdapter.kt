package com.example.flo

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MyAdapter(val context: Context, val lyrics: ArrayList<String>) : BaseAdapter() {
    override fun getView(positon: Int, p1: View?, p2: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.custom_layout, null)
        val mLyrics = view.findViewById<TextView>(R.id.lyricsLine)

        mLyrics.text = lyrics.get(positon)
        if(positon == FLOdata.timeindex){
            mLyrics.setTextColor(Color.parseColor("#3E3AFF"))
        }
        else mLyrics.setTextColor(Color.parseColor("#FFFFFF"))
        return view
    }

    override fun getItem(p0: Int): Any {
        return lyrics[p0]
    }

    override fun getItemId(position: Int): Long {
        return 0//not use
    }


    override fun getCount(): Int {
        return lyrics.size
    }

}