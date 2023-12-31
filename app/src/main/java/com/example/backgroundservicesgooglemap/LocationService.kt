package com.example.backgroundservicesgooglemap

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID=12345
    }

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationCallback: LocationCallback? = null
    var locationRequest: LocationRequest? = null
    var notificationManager: NotificationManager? = null
    var location:Location? =null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(500).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }

        }
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "location", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }
    @Suppress("Missingpermission")
    fun createLocationRequest(){
        try {
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest!!,locationCallback!!,null)
        }catch (e:Exception){
            Log.v("exception","$e")
        }
    }
    fun removeLocationUpdates(){
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(true)
        stopSelf()
    }

    private fun onNewLocation(locationResult: LocationResult) {
        location=locationResult.lastLocation
        EventBus.getDefault().post(locationEvent(
            latitude =location!!.latitude,
            longitude = location!!.longitude
        ))

        startForeground(NOTIFICATION_ID,getNotification())


    }
    fun getNotification():Notification{
        val notification=NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("location update")
            .setContentText("Latitude-->${location?.latitude}\nLongitude-->${location?.longitude}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notification.setChannelId(CHANNEL_ID)
        }
        return notification.build()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }
}