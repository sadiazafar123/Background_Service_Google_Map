package com.example.backgroundservicesgooglemap

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {
    lateinit var tvLatitude:TextView
    lateinit var tvLongitude:TextView
    lateinit var btnStart:Button
    lateinit var btnEnd:Button
    private var service:Intent?=null

        private val backgroundLocation=registerForActivityResult(ActivityResultContracts.
        RequestPermission()){
            if (it){

            }

        }
    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                        }
                    }
                }
                it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                }

            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLatitude=findViewById(R.id.tvLatitude)
        tvLongitude=findViewById(R.id.tvLongitude)
        btnStart=findViewById(R.id.btnStart)
        btnEnd=findViewById(R.id.btnEnd)
        service=Intent(this,LocationService::class.java)

        btnStart.setOnClickListener(){
            checkPermission()
        }
        btnEnd.setOnClickListener(){
            stopService(service)

        }

    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this
            )
        }
    }
    @Subscribe
    fun receiveLocationEvent(locationEvent: locationEvent){
        tvLatitude.text=locationEvent.latitude.toString()
        tvLongitude.text=locationEvent.longitude.toString()


    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissions.launch(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                )

            }

        }else{
            startService(service)

        }
    }
}