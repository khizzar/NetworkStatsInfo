package com.khizzar.statssdk

import android.Manifest
import android.app.ActivityManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.khizzar.statssdk.interfaces.StatsSDKListener
import com.khizzar.statssdk.models.InternalSessionModel
import com.khizzar.statssdk.models.SessionDataModel
import com.khizzar.statssdk.netStat.DataUsageManager
import com.khizzar.statssdk.netStat.Usage
import com.khizzar.statssdk.network.ApiCalls
import com.khizzar.statssdk.prefs.Prefs
import com.khizzar.statssdk.utils.*
import java.io.File
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.max


/**
 * Created By: Norgic
 * Date & Time: On 7/26/21 At 1:07 PM in 2021
 */
class StatsClient private constructor(private val context: Context) {

    private val prefs: Prefs = Prefs(context)
    private val apiCall: ApiCalls = ApiCalls(context)
    private var listener: StatsSDKListener? = null
    private var bm: BatteryManager
    private var manager: DataUsageManager
    var mi: ActivityManager.MemoryInfo? = null
    var activityManager: ActivityManager? = null

    private var activeSessionsMaps: HashMap<String, InternalSessionModel> = HashMap()

    lateinit var networkStatsManager: NetworkStatsManager
    private lateinit var memoryUsageExecutor: ScheduledThreadPoolExecutor

    var applicationUID = 0
    var availableMegs: Long = 0L

    init {
        bm = context.getSystemService(AppCompatActivity.BATTERY_SERVICE) as BatteryManager
        applicationUID = context.applicationInfo.uid

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            networkStatsManager =
                context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        }

        manager = DataUsageManager(networkStatsManager)

        mi = ActivityManager.MemoryInfo()
        activityManager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        getTotalAvailableRam()
    }

    fun setListener(statsCallbacks: StatsSDKListener) {
        this.listener = statsCallbacks
    }

    fun initiateCallLogsHandler(sessionKey: String) {
        val executor = ScheduledThreadPoolExecutor(1)
        val initialDelay = 0L


        val internalSessionModel = InternalSessionModel(
            startUsage = manager.getDataUsage(applicationUID),
            realtimeUsage = manager.getDataUsage(applicationUID),
            startTime = System.currentTimeMillis(),
            initialBatteryUsage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
            executor = executor
        )
        // save the data to the hash map
        activeSessionsMaps[sessionKey] = internalSessionModel

//        start the executor
        prefs.timeInterval.toLong().let { interval ->
            executor.scheduleWithFixedDelay(
                { sendDuringCallData() },
                initialDelay,
                interval,
                TimeUnit.SECONDS
            )
        }
    }

    fun shutDownCallLogsHandler(sessionKey: String) {
//        get the end data
        val endUsage = manager.getDataUsage(applicationUID)
        val endBatteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

//        get the session from map update the values
        val sessionModel = activeSessionsMaps[sessionKey]

//        stop the executor
        sessionModel?.executor?.let { executor ->
            if (!executor.isShutdown || executor.isTerminated) executor.shutdownNow()
        }

//        send end session data
        sessionModel?.let { internalSessionModel ->
            val session = SessionDataModel(
                user_type = "",
                join_time = internalSessionModel.startTime.toString(),
                left_time = System.currentTimeMillis().toString(),
                device_model = getDeviceName(),
                device_os = Build.VERSION.SDK_INT.toString(),
                local_ip = getLocalIp(context),
                public_ip = apiCall.currentPublicIp,
                battery_usage = if (internalSessionModel.initialBatteryUsage - endBatteryLevel == 0) "0"
                else (internalSessionModel.initialBatteryUsage - endBatteryLevel).toString(),
                total_uploaded_bytes = (endUsage.uploads - internalSessionModel.startUsage?.uploads!!).toString(),
                total_downloaded_bytes = (endUsage.downloads - internalSessionModel.startUsage?.downloads!!).toString()
            )
            listener?.sendEndDataUsage(session)
            saveLogsToFile(session)
        }

//        remove the session model from the map
        activeSessionsMaps.remove(sessionKey)
    }

    private fun saveLogsToFile(sessionModel: SessionDataModel) {
        prefs.enableLogsWriting?.let { isEnabled ->
            if (isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    downloadFile(context, sessionModel)
                } else {
                    writeCallLogToFile(sessionModel)
                }
            }
        }
    }

    private fun sendDuringCallData() {
        val itr = activeSessionsMaps.iterator()
        while (itr.hasNext()) {

//            get the session model
            var sessionModel: Map.Entry<String, InternalSessionModel> = itr.next()

//            get the current usage
            val currentUsage = manager.getDataUsage(applicationUID)

//            calculate the values
            val currentUsageDownload =
                currentUsage.downloads - sessionModel.value.realtimeUsage?.downloads!!
            val currentUsageUpload =
                currentUsage.uploads - sessionModel.value.realtimeUsage?.uploads!!

            Log.e(
                "session_logs", "currentUsage: $currentUsage," +
                        " realtimeUsage: ${sessionModel.value.realtimeUsage}," +
                        " downloads: $currentUsageDownload," +
                        " uploads: $currentUsageUpload" +
                        " sessionKey: ${sessionModel.key}"
            )

//            update the session model
            sessionModel.value.realtimeUsage = currentUsage
            activeSessionsMaps[sessionModel.key] = sessionModel.value

//            send callback of the current data
            listener?.sendCurrentDataUsage(
                sessionModel.key,
                Usage(
                    downloads = currentUsageDownload,
                    uploads = currentUsageUpload
                )
            )
        }
    }

    fun initiateMemoryUsageLogs(startDelay: Long = 0L, timeDelayInSecs: Long) {
        memoryUsageExecutor = ScheduledThreadPoolExecutor(1)
        memoryUsageExecutor.scheduleWithFixedDelay(
            {
                activityManager?.getMemoryInfo(mi)
                val currentMemoryAvailable = mi?.availMem?.div(1048576L) ?: 0L
                Log.e("memory usage", "" + (availableMegs - currentMemoryAvailable))
                listener?.memoryUsageDetails(availableMegs - currentMemoryAvailable)
            },
            startDelay,
            timeDelayInSecs,
            TimeUnit.SECONDS
        )
    }

    fun stopMemoryUsageLogs() {
        if (!memoryUsageExecutor.isShutdown || !memoryUsageExecutor.isTerminated) memoryUsageExecutor.shutdown()
    }

    private fun getTotalAvailableRam() {
        activityManager?.getMemoryInfo(mi)
        availableMegs = mi?.availMem?.div(1048576L) ?: 0L
        Log.e("available memory", "" + availableMegs)
    }

    fun getNumberOfCpuCores(): Int {
        val pattern = Pattern.compile("cpu[0-9]+")
        return max(
            File("/sys/devices/system/cpu/")
                .walk()
                .maxDepth(1)
                .count { pattern.matcher(it.name).matches() },
            Runtime.getRuntime().availableProcessors()
        )
    }

    fun setConstants(timeInterval: Int) {
        prefs.timeInterval = timeInterval
    }

    /**
     * This function will enable log writing in a file
     * please be sure to provide READ/WRITE permissions before using it
     * @param enableWriteLogs boolean to enable disable call logs writing
     * */
    fun enableWritingLogs(enableWriteLogs: Boolean) {
        prefs.enableLogsWriting = enableWriteLogs
        if (enableWriteLogs) createInitialFileForLogs()
    }

    private fun createInitialFileForLogs() {
        if ((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED)
        ) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                makeFileInStorage()
            }
        }
    }

    companion object : SingletonHolder<StatsClient, Context>(::StatsClient)

}

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T? {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator?.let { it(arg) }
                instance = created
                creator = null
                created
            }
        }
    }
}