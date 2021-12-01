package com.projectc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivityC : AppCompatActivity() {

    companion object {
        fun test() {
            Log.e("lzy", "test: 1111")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        Log.e("lzy", "onCreate: 1111")
        println("sf")
    }
}