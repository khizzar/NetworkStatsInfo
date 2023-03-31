package com.khizzar.statssdk.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


/**
 * Created By: Norgic
 * Date & Time: On 5/5/21 At 12:57 PM in 2021
 */
interface ApiService {

    @GET
    suspend fun getPublicIP(@Url url: String?) : Response<String>

}