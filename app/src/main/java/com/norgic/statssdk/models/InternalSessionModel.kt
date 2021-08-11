package com.norgic.statssdk.models

import android.os.Parcelable
import com.norgic.statssdk.netStat.Usage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.concurrent.ScheduledThreadPoolExecutor


/**
 * Created By: Norgic
 * Date & Time: On 7/29/21 At 12:44 PM in 2021
 */
@Parcelize
data class InternalSessionModel(
    var startUsage: Usage? = Usage(0L, 0L),
    var realtimeUsage: Usage? = Usage(0L, 0L),
    var startTime: Long = 0L,
    var initialBatteryUsage: Int = 0,
    var executor: @RawValue ScheduledThreadPoolExecutor? = null
): Parcelable
