package com.ezshifa.aihealthcare.mobilesdk

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ezshifa.aihealthcare.ezdiagssdk.EZDiags

class MainActivity : AppCompatActivity() {

    private lateinit var btnScanning : Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanning = findViewById(R.id.btnScanning)

        btnScanning.setOnClickListener {
            EZDiags.startScanning(this@MainActivity, "sdktesting")
        }

    }
}