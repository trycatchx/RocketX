package com.rocketx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.projecta.MainActivityA

class MainActivityApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun test(v: View) {
        MainActivityA().test1()
    }
}