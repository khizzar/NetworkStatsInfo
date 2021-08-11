package com.norgic.statssdk.network

import android.content.Context
import android.util.Log
import com.norgic.statssdk.utils.PUBLIC_IP_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


/**
 * Created By: Norgic
 * Date & Time: On 7/28/21 At 12:41 PM in 2021
 */
class ApiCalls(private val context: Context) {

    var currentPublicIp: String = ""

    init {
        getPublicIp()
    }

    fun getPublicIp(): String {
        val service =
            RetrofitBuilder.makeRetrofitServiceForPublicIp(context)
        CoroutineScope(Dispatchers.IO).launch {
            val response = safeApiCall {
                service.getPublicIP(PUBLIC_IP_URL)
            }
            withContext(Dispatchers.Main) {
                try {
                    when (response) {
                        is Result.Success -> {
                            Log.e("publicIP", "getPublicIpSuccess: $response")
                            currentPublicIp = response.data
                        }
                        is Result.Error -> {
                            Log.e("publicIP", "getPublicIpError: $response")
                        }
                    }
                } catch (e: HttpException) {
                    Log.e("publicIP", "exception: ${e.message()}")
                } catch (e: Throwable) {
                    Log.e("publicIP", "exception: ${e.message}")
                }
            }
        }
        return currentPublicIp
    }

}