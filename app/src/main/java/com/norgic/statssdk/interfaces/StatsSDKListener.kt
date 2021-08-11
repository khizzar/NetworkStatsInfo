package com.norgic.statssdk.interfaces

import com.norgic.statssdk.models.SessionDataModel
import com.norgic.statssdk.netStat.Usage


/**
 * Created By: Norgic
 * Date & Time: On 7/26/21 At 1:15 PM in 2021
 */
interface StatsSDKListener {
    fun sendCurrentDataUsage(sessionKey: String, usage: Usage)
    fun sendEndDataUsage(sessionDataModel: SessionDataModel)
    fun memoryUsageDetails(memoryUsage: Long)
}