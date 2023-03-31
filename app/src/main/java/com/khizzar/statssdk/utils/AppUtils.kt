package com.khizzar.statssdk.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.RandomAccessFile


fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        model.toUpperCase()
    } else manufacturer.toUpperCase() + " " + model
}

fun getLocalIp(context: Context): String {
    val wifiManager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
    return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
}

//function only works for android 7 and lower
private fun readUsage(): Float {
    try {
        val reader = RandomAccessFile("/proc/stat", "r")
        var load = reader.readLine()
        var toks = load.split(" ".toRegex()).toTypedArray()
        val idle1 = toks[5].toLong()
        val cpu1 =
            toks[2].toLong() + toks[3].toLong() + toks[4].toLong() + toks[6].toLong() + toks[7].toLong() + toks[8].toLong()
        try {
            Thread.sleep(360)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        reader.seek(0)
        load = reader.readLine()
        reader.close()
        toks = load.split(" ".toRegex()).toTypedArray()
        val idle2 = toks[5].toLong()
        val cpu2 =
            toks[2].toLong() + toks[3].toLong() + toks[4].toLong() + toks[6].toLong() + toks[7].toLong() + toks[8].toLong()
        return (cpu2 - cpu1).toFloat() / (cpu2 + idle2 - (cpu1 + idle1))
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return 0F
}
