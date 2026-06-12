package com.eim.callapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eim.callapp.databinding.ActivityActiveCallBinding

class ActiveCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEndCall.setOnClickListener { finish() }
    }
}
