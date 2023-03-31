package com.khizzar.statssdk.netStat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Usage(var downloads: Long = 0L, var uploads: Long = 0L): Parcelable