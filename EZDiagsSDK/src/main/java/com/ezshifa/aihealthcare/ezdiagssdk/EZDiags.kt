package com.ezshifa.aihealthcare.ezdiagssdk

import android.content.Context
import android.content.Intent

class EZDiags {

    companion object {
        fun startScanning(context: Context, userName : String) {
            try {
                val intent = Intent(Intent(context, VideoScanningActivity::class.java))
                intent.putExtra("username", userName)
                context.startActivity(intent)
            }catch (ex : Exception){
                ex.printStackTrace()
            }
        }

    }
}