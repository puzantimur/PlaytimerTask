package com.example.playtimetask

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tracker = PlaytimeTracker()
        tracker.init(context = applicationContext, lifecycleOwner = this)
        lifecycleScope.launch {
            tracker.playtimeState.collect {
                Log.d("TAG", it.toString())  /** via this log u can check how it works.*/
            }
        }
    }
}