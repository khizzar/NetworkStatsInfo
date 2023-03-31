package com.khizzar.statssdk.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * Created By: Norgic
 * Date & Time: On 7/28/21 At 11:51 AM in 2021
 */
@Parcelize
data class SessionDataModel(
    var type: String = "data",
    var device_category: String = "mobile",
    var device_type: String = "android",
    var user_type: String = "",
    var join_time: String = "",
    var left_time: String = "",
    var device_model: String = "",
    var device_os: String = "",
    var local_ip: String = "",
    var public_ip: String = "",
    var battery_usage: String = "",
    var total_uploaded_bytes: String = "",
    var total_downloaded_bytes: String = "",
): Parcelable