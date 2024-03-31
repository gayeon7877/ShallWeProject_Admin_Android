package com.shall_we.admin.login.retrofit

import android.util.Log
import com.shall_we.admin.login.data.AuthRes
import com.shall_we.admin.login.data.RefreshTokenReq
import com.shall_we.admin.retrofit.API
import com.shall_we.admin.retrofit.IRetrofit
import com.shall_we.admin.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenRefreshService() {
    companion object {
        val instance = TokenRefreshService()
    }
    private val iRetrofit: IRetrofit? =
        RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)
    suspend fun tokenRefresh(refreshToken: RefreshTokenReq): Response<AuthRes> {
        return iRetrofit?.tokenRefresh(refreshToken) ?: error("IRetrofit is not initialized")
    }
}