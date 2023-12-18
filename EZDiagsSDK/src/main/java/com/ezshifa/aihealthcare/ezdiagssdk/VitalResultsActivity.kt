package com.ezshifa.aihealthcare.ezdiagssdk

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezshifa.aihealthcare.ezdiagssdk.databinding.ActivityVitalResultsBinding
import com.ezshifa.aihealthcare.ezdiagssdk.network.ApiUtils
import com.ezshifa.aihealthcare.ezdiagssdk.util.GeneralUtil
import com.ezshifa.aihealthcare.models.vitalshistoryapiresponsemodels.VitalApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class VitalResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVitalResultsBinding
    private lateinit var oxygen : String
    private lateinit var heartRate : String
    private lateinit var bloodPressure : String
    private lateinit var temperature : String
    private lateinit var sugar : String
    private lateinit var bps : String
    private lateinit var bpd : String
    private var hrValue : Int = 0
    private var bpValue : Int = 0
    private var tempValue : Int = 0
    private var oxygenValue : Int = 0
    private val range1 : Int = 1
    private val range2 : Int = 2
    private val range3 : Int = 3
    private val range4 : Int = 4


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVitalResultsBinding.inflate(layoutInflater)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        initViews()
        clickListeners()

    }

    private fun initViews() {
        
        val intent = intent
        val fileName = intent.getStringExtra("fileName").toString()
        val videoId = intent.getStringExtra("videoId").toString()
        binding.clWaitingForResults.visibility = View.VISIBLE
        binding.clVitalsResult.visibility = View.GONE
        if (GeneralUtil.isInternetConnected(this@VitalResultsActivity)) {
            callApi(fileName, videoId)
        }else{
            callApi(fileName, videoId)
            toast(getString(R.string.connect_to_internet))
        }
    }

    private fun clickListeners() {
        binding.exit.setOnClickListener {
            finish()
        }

        // range charts
        binding.tvOxygenRangeChart.setOnClickListener {
            showRangeChart("oxygen")
        }

        binding.tvTemperatureChart.setOnClickListener {
            showRangeChart("temperature")
        }

        binding.tvBPChart.setOnClickListener {
            showRangeChart("bp")
        }

        binding.tvHRChart.setOnClickListener {
            showRangeChart("hr")
        }

        binding.tvSugarChart.setOnClickListener {
            showSugarRangeChart()
        }

    }

    fun callApi(fileName : String, videoId : String){

        ApiUtils.getAPIService(this@VitalResultsActivity)
            .getVitalsResults(fileName, videoId.toInt(), VideoScanningActivity.userName.toString())
            .enqueue(object : Callback<VitalApiResponse> {
                override fun onFailure(call: Call<VitalApiResponse>, t: Throwable) {
                    toast(getString(R.string.default_error_message)+" "+t.message.toString())
                    finish()
                }

                override fun onResponse(
                    call: Call<VitalApiResponse>,
                    response: Response<VitalApiResponse>
                ) {
                    if (response.code().equals(200)){
                        if (response.isSuccessful && response.body() != null){
                            if (response.body()!!.statusCode == 200 && response.body()!!.vitals != null){

                                val apiResponse = response.body()!!
                                if (apiResponse.vitals!!.oxygen != null && apiResponse.vitals!!.bp != null && apiResponse.vitals!!.temperature != null
                                    && apiResponse.vitals!!.pulse != null && apiResponse.vitals!!.sugar != null){

                                    oxygen = apiResponse.vitals!!.oxygen!!
                                    bloodPressure = apiResponse.vitals!!.bp!!
                                    heartRate = apiResponse.vitals!!.pulse!!
                                    temperature = apiResponse.vitals!!.temperature!!
                                    sugar = apiResponse.vitals!!.sugar!!
                                    bps = apiResponse.vitals!!.bps!!
                                    bpd = apiResponse.vitals!!.bpd!!

                                    // show results on screen
                                    binding.clWaitingForResults.visibility = View.GONE
                                    binding.clVitalsResult.visibility = View.VISIBLE
                                    getAndSetValues()

                                }else{
                                    Toast.makeText(this@VitalResultsActivity, getString(R.string.vital_fetched_unsuccessful_please_try_again), Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            }
                            else{
                                Toast.makeText(this@VitalResultsActivity, response.body()!!.message.toString(), Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                    else{
                        Toast.makeText(this@VitalResultsActivity, getString(R.string.default_error_message), Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            })
    }

    private fun getAndSetValues() {

        try {
            if (heartRate.isEmpty() && bloodPressure.isEmpty() && temperature.isEmpty() && oxygen.isEmpty() && sugar.isEmpty()) {
                binding.oxygenval.text = "00"
                binding.tempval.text = "00 \u2109"
                binding.heartrateval.text = "00"
                binding.bpval.text = "00"
                binding.sugarval.text = "00"
            } else {
                binding.oxygenval.text = oxygen
                binding.tempval.text = temperature +" ℉"
                binding.heartrateval.text = heartRate
                binding.bpval.text = bloodPressure
                binding.sugarval.text = sugar

                try {
                    // heart rate
                    if (heartRate.toInt() in 60..70){
                        hrValue = range4
                    }
                    else if (heartRate.toInt() in 71..90){
                        hrValue = range3
                    }
                    else if (heartRate.toInt() in 91..100){
                        hrValue = range2
                    }
                    else if (heartRate.toInt() > 100){
                        hrValue = range1
                    }

                    // temperature
                    if (temperature.toInt() in 96..98){
                        tempValue = range4
                    }
                    else if (temperature.toInt() in 95..96){
                        tempValue = range3
                    }
                    else if (temperature.toInt() in 91..95){
                        tempValue = range2
                    }
                    else if (temperature.toInt() < 91){
                        tempValue = range1
                    }

//                        // blood pressure
                    if (bps.toInt() in 110..120 && bpd.toInt() in 70..80){
                        bpValue = range4
                    }
                    else if (bps.toInt() in 120..125 && bpd.toInt() in 80..85){
                        bpValue = range3
                    }
                    else if (bps.toInt() in 135..140 && bpd.toInt() in 85..90){
                        bpValue = range2
                    }
                    else if (bps.toInt() in 140..180 && bpd.toInt() in 80..95){
                        bpValue = range1
                    }

//                        // oxygen
                    if (oxygen.toInt() in 96..98){
                        oxygenValue = range4
                    }
                    else if(oxygen.toInt() in 96..97){
                        oxygenValue = range3
                    }
                    else if (oxygen.toInt() in 90..95){
                        oxygenValue = range2
                    }
                    else if (oxygen.toInt() in 88..90){
                        oxygenValue = range1
                    }

                    val stressLevel = ( (hrValue + tempValue + bpValue + oxygenValue) / 4 )
                    if (stressLevel.equals(4)){
                        binding.tvStressLevelValue.text = getString(R.string.relax)
                        binding.tvWellnessScore.text = "9/10"
                        binding.seekbarWellnessScore.progress = 9
                    }
                    else if (stressLevel.equals(3)){
                        binding.tvStressLevelValue.text = getString(R.string.calm)
                        binding.tvWellnessScore.text = "7/10"
                        binding.seekbarWellnessScore.progress = 7
                    }
                    else if (stressLevel.equals(2)){
                        binding.tvStressLevelValue.text = getString(R.string.anxious)
                        binding.tvWellnessScore.text = "5/10"
                        binding.seekbarWellnessScore.progress = 5
                    }
                    else if (stressLevel.equals(1)){
                        binding.tvStressLevelValue.text = getString(R.string.stressed)
                        binding.tvWellnessScore.text = "3/10"
                        binding.seekbarWellnessScore.progress = 3
                    }
                    else{
                        binding.tvStressLevelValue.text = getString(R.string.stressed)
                        binding.tvWellnessScore.text = "3/10"
                        binding.seekbarWellnessScore.progress = 3
                    }
                }catch (ex : Exception){
                    ex.printStackTrace()
                }
            }

        }catch (ex:Exception){
            ex.printStackTrace()
        }

    }

    fun toast(message: String) {
        Toast.makeText(this@VitalResultsActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRangeChart(rangeChartOf : String) {
        val dialog = Dialog(this@VitalResultsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.range_cahrt_layout)
        dialog.show()
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        val ivRangeChart : ImageView = dialog.findViewById(R.id.ivRangeChart)
        val btnCancel : ImageView = dialog.findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener { dialog.dismiss() }

        if (rangeChartOf.equals("oxygen")){
            ivRangeChart.setImageResource(R.drawable.oxygen_rangechart)
        }else if (rangeChartOf.equals("temperature")){
            ivRangeChart.setImageResource(R.drawable.temp_rangechart)
        }else if (rangeChartOf.equals("bp")){
            ivRangeChart.setImageResource(R.drawable.bp_rangechart)
        }else if (rangeChartOf.equals("hr")){
            ivRangeChart.setImageResource(R.drawable.hr_rangechart)
        }

    }

    private fun showSugarRangeChart() {
        val dialog = Dialog(this@VitalResultsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.sugar_rangechart_layout)
        dialog.show()
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        val btnCancel : ImageView = dialog.findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener { dialog.dismiss() }

    }

}