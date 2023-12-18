package com.ezshifa.aihealthcare.ezdiagssdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.ezshifa.aihealthcare.ezdiagssdk.databinding.ActivityVideoScanningBinding
import com.ezshifa.aihealthcare.ezdiagssdk.network.ApiUtils
import com.ezshifa.aihealthcare.ezdiagssdk.responsemodels.UploadVideoResponse
import com.ezshifa.aihealthcare.ezdiagssdk.util.GeneralUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoScanningActivity : AppCompatActivity() {

    private lateinit var binding : ActivityVideoScanningBinding

    private lateinit var file : File
    private var isVideoUploaded = false
    var mPDialog: ProgressDialog? = null

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var videoCapture: VideoCapture? = null

    private val MULTIPLE_PERMISSIONS = 10
    var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    companion object{
        lateinit var userName : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoScanningBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)
        
        initViews()

    }

    private fun initViews() {

        val intent = intent
        userName = intent.getStringExtra("username").toString()

        if (checkPermissions()) {
            startCamera()
        }
        
        clickListener()

    }


    private fun clickListener() {

        binding.btnCaptureUserVideo.setOnClickListener(View.OnClickListener {
            file = File(this.filesDir.toString() + "/vitalVideo.mp4")
            binding.btnCaptureUserVideo.isClickable = false
            try {
                if (file.exists()) {
                    file.delete()
                    file.createNewFile()
                    if (file.exists()) {
                        startRecording(file)
                    }else{
                        Toast.makeText(this, getString(R.string.file_creation_failed), Toast.LENGTH_SHORT).show()
                    }

                } else {
                    file.createNewFile()
                    if (file.exists()){
                        startRecording(file)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Exception : $e", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnCancelVideo.setOnClickListener {
            binding.btnCaptureUserVideo.visibility = View.VISIBLE
            binding.tvWaiting.visibility = View.INVISIBLE
            binding.tvSystemStatus.visibility = View.VISIBLE
            binding.tvTimeCounter.visibility = View.GONE
            binding.clReloadLayout.visibility = View.GONE
        }

        binding.btnReloadVideo.setOnClickListener {

            binding.btnCaptureUserVideo.isClickable = false
            binding.clReloadLayout.visibility = View.GONE
            binding.tvTimeCounter.visibility = View.VISIBLE
            binding.btnCaptureUserVideo.visibility = View.VISIBLE
            binding.tvSystemStatus.visibility = View.GONE
            binding.tvTimeCounter.visibility = View.VISIBLE

            file = File(this.filesDir.toString() + "/vitalVideo.mp4")
            try {
                if (file.exists()) {
                    file.delete()
                    file.createNewFile()
                    startRecording(file)
                } else {
                    file.createNewFile()
                    if (file.exists()){
                        startRecording(file)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Exception : $e", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOK.setOnClickListener {

            val fileSizeInBytes: Long = file.length()
            val fileSizeInKB: Long = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024

            if (fileSizeInMB > 0){
                mPDialog =  ProgressDialog(this@VideoScanningActivity)
                mPDialog!!.setMessage(getString(R.string.working_on_video_please_wait))
                mPDialog!!.setIndeterminate(true)
                mPDialog!!.setCancelable(false)
                mPDialog!!.show()
                val newFile = File(this.filesDir.toString() + "/newVitalVideo.mp4")
                if (newFile.exists()) {
                    newFile.delete()
                    newFile.createNewFile()
                } else {
                    newFile.createNewFile()
                }

                try {

                    GPUMp4Composer(file.absolutePath, newFile.absolutePath)
                        .size(720, 1280)
                        .listener(object : GPUMp4Composer.Listener {
                            override fun onProgress(progress: Double) {

                            }

                            override fun onCompleted() {
                                runOnUiThread {
                                    val fileSizeInBytes1: Long = newFile.length()
                                    val fileSizeInKB1: Long = fileSizeInBytes1 / 1024
                                    val fileSizeInMB1 = fileSizeInKB1 / 1024
                                    if (fileSizeInMB1 > 0) {
                                        // upload video
                                        uploadRecordingVideo(newFile.absolutePath)
                                    }else{
                                        runOnUiThread {
                                            mPDialog?.dismiss()
                                            Toast.makeText(
                                                this@VideoScanningActivity,
                                                getString(R.string.video_capturing_error_please_try_again),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }

                            override fun onCanceled() {
                                runOnUiThread {
                                    mPDialog?.dismiss()
                                    Toast.makeText(
                                        this@VideoScanningActivity,
                                        getString(R.string.video_capturing_error_please_try_again),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onFailed(exception: Exception) {
                                runOnUiThread {
                                    mPDialog?.dismiss()
                                    Toast.makeText(
                                        this@VideoScanningActivity,
                                        getString(R.string.video_capturing_error_please_try_again),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                        .start()

                }catch (ex : Exception){
                    mPDialog?.dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.video_capturing_error_please_try_again),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }else{
                Toast.makeText(this, getString(R.string.video_capturing_error_please_try_again), Toast.LENGTH_LONG).show()
            }

        }


    }

    @SuppressLint("RestrictedApi")
    private fun startRecording(videoFile: File) {

        val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        try {
            binding.tvSystemStatus.visibility = View.GONE
            binding.tvWaiting.visibility = View.VISIBLE
            binding.tvTimeCounter.visibility = View.VISIBLE
            countdowntimer()
            videoCapture?.startRecording(
                outputOptions,
                cameraExecutor,
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        cause?.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@VideoScanningActivity, message, Toast.LENGTH_SHORT).show()
                            binding.btnCaptureUserVideo.visibility = View.VISIBLE
                            binding.tvWaiting.visibility = View.INVISIBLE
                            binding.tvSystemStatus.visibility = View.VISIBLE
                            binding.tvTimeCounter.visibility = View.GONE
                            binding.clReloadLayout.visibility = View.GONE
                        }
                    }
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        // Video saved successfully
                        runOnUiThread {
                            binding.tvWaiting.visibility = View.GONE
                            binding.btnCaptureUserVideo.visibility = View.INVISIBLE
                            binding.clReloadLayout.visibility = View.VISIBLE
                        }
                    }
                })
        }catch (ex : Exception){
            ex.printStackTrace()
        }

    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@VideoScanningActivity)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setDefaultResolution(Size(720, 1280))
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            videoCapture = VideoCapture.Builder()
                .setVideoFrameRate(30)
                .setDefaultResolution(Size(720, 1280))
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this@VideoScanningActivity))
    }

    private fun uploadRecordingVideo(videoPath : String){

        val file = File(videoPath)
        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("fileName", file.name, requestFile)
        val filename = RequestBody.create("filename".toMediaTypeOrNull(), "video")
        if (GeneralUtil.isInternetConnected(this@VideoScanningActivity)) {
            sendFileUploadRequest(body,filename)
        }else{
            mPDialog!!.dismiss()
            Toast.makeText(this@VideoScanningActivity, getString(R.string.connect_to_internet), Toast.LENGTH_SHORT).show()
        }

    }

    private fun sendFileUploadRequest(
        file: MultipartBody.Part,
        filename: RequestBody
    ) {

        try {

            val apiCall : Call<UploadVideoResponse> = ApiUtils.getAPIService(this@VideoScanningActivity)
                .UploadVideo(file, userName)

            apiCall.enqueue(object : Callback<UploadVideoResponse> {
                override fun onFailure(call: Call<UploadVideoResponse>, t: Throwable) {
                    Toast.makeText(this@VideoScanningActivity, getString(R.string.server_error), Toast.LENGTH_SHORT).show()
                    mPDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UploadVideoResponse>,
                    response: Response<UploadVideoResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        mPDialog!!.dismiss()
                        isVideoUploaded = true
                        if (response.body()!!.statusCode!!.equals(200)){
                            // store the param and forward it
                            Toast.makeText(this@VideoScanningActivity, getString(R.string.video_uploaded_successfully), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@VideoScanningActivity, VitalResultsActivity::class.java)
                            intent.putExtra("fileName", response.body()!!.data!!.filename.toString())
                            intent.putExtra("videoId", response.body()!!.data!!.vid.toString())
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this@VideoScanningActivity, getString(R.string.server_error), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        mPDialog!!.dismiss()
                        Toast.makeText(
                            this@VideoScanningActivity,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            })

            object : CountDownTimer(150000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }
                // When the task is over it will print 00:00:00 there
                @SuppressLint("RestrictedApi")
                override fun onFinish() {
                    if (!isVideoUploaded){
                        mPDialog!!.dismiss()
                        apiCall.cancel()
                    }
                }
            }.start()

        }catch (ex : Exception){
            mPDialog!!.dismiss()
            Toast.makeText(this@VideoScanningActivity, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }


    }

    private fun countdowntimer(){
        object : CountDownTimer(16000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.tvTimeCounter.setText(f.format(min) + ":" + f.format(sec) + " secs")
            }
            // When the task is over it will print 00:00:00 there
            @SuppressLint("RestrictedApi")
            override fun onFinish() {
                try {
                    videoCapture?.stopRecording()
                    binding.btnCaptureUserVideo.isClickable = true
                    binding.tvTimeCounter.setText("00:00")
                }catch (ex : Exception){
                    ex.printStackTrace()
                }
            }
        }.start()
    }

    private fun checkPermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= 33){
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }

        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do code here
                    startCamera()
                } else {
                    val builder = AlertDialog.Builder(this@VideoScanningActivity).apply {
                        setTitle(getString(R.string.permissions_required))
                        setMessage(getString(R.string.please_allow_all_the_permissions_to_continue))
                        setCancelable(false)
                    }
                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvWaiting.visibility = View.INVISIBLE
        binding.tvSystemStatus.visibility = View.VISIBLE
        binding.btnCaptureUserVideo.visibility = View.VISIBLE
        binding.clReloadLayout.visibility = View.INVISIBLE
        binding.tvTimeCounter.visibility = View.GONE
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        super.onPause()
        try {
            videoCapture?.stopRecording()
        }catch (ex : Exception){
            ex.printStackTrace()
        }
    }
    
}