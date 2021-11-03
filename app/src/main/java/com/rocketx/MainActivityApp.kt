package com.rocketx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.projecta.MainActivityA
import com.tcl.annotation.CellName

@CellName
class MainActivityApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun test(v: View) {
        if(v != null)
        MainActivityA().test1()
    }
}