package com.projectc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.projectc.databinding.ActivityProjectcBinding

class MainActivityC : AppCompatActivity() {

    companion object {
        fun test() {
            Log.e("lzy", "test: 1111")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val mBinding = ActivityProjectcBinding.inflate(getLayoutInflater())
        setContentView(mBinding.getRoot())
        println("sf")
    }
}