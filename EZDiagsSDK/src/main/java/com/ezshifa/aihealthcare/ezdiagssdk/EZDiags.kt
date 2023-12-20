package com.ezshifa.aihealthcare.ezdiagssdk

import android.content.Context
import android.content.Intent
import android.widget.Toast

class EZDiags {

    companion object {
        fun startScanning(context: Context, userName : String, userKey : String) {
            try {
                if (userName.isNotEmpty() && userName.length > 2) {
                    val intent = Intent(Intent(context, VideoScanningActivity::class.java))
                    intent.putExtra("username", userName)
                    intent.putExtra("userKey", userKey)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context, context.getString(R.string.valid_username_required), Toast.LENGTH_SHORT).show()
                }
            }catch (ex : Exception){
                ex.printStackTrace()
            }
        }

    }
}