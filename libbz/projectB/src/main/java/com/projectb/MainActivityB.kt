package com.projectb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivityB : AppCompatActivity() {
    companion object {
        fun test() {
            Log.e("lzy", "test MainActivityB  8888")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }
}