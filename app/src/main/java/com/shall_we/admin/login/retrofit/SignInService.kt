package com.shall_we.admin.login.retrofit

import android.util.Log
import com.google.gson.Gson
import com.shall_we.admin.login.data.AuthRes
import com.shall_we.admin.login.data.ErrorRes
import com.shall_we.admin.login.data.SignInReq
import com.shall_we.admin.retrofit.API
import com.shall_we.admin.retrofit.IRetrofit
import com.shall_we.admin.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInService (val authSignInterface: IAuthSignIn) {

    private val iRetrofit: IRetrofit? =
        RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun postAuthSignIn(auth: SignInReq){
        iRetrofit?.authSignIn(SignInReq(auth.phoneNumber,auth.password))?.enqueue(object:
            Callback<AuthRes> {
            override fun onResponse(call: Call<AuthRes>, response: Response<AuthRes>) {
                if(response.code() == 200){
                    val authResponse = response.body()
                    if (authResponse != null) {
                        Log.e("login", "Success: ${authResponse}")
                        authSignInterface.onPostAuthSignInSuccess(authResponse)
                    } else {
                        authSignInterface.onPostAuthSignInFailed("응답 데이터를 받아올 수 없습니다.")
                    }
                }
                else if(response.code() == 400){
                    val gson = Gson()
                    val errorResponse =
                        gson.fromJson(response.errorBody()?.string(), ErrorRes::class.java)
                    Log.e("login", "Request failed with response code: ${errorResponse.data.message}")

                    authSignInterface.onPostAuthSignInFailed(errorResponse.data.message)
                    Log.e("login", "Request failed with response code: ${errorResponse.data.message}")

                }
                else{
                    try{
                        val gson = Gson()
                        val errorResponse =
                            gson.fromJson(response.errorBody()?.string(), ErrorRes::class.java)
                        authSignInterface.onPostAuthSignInFailed(errorResponse.data.message)
                    }catch(e:Exception){
                        authSignInterface.onPostAuthSignInFailed(e.message?:"통신 오류")
                    }
                }
            }

            override fun onFailure(call: Call<AuthRes>, t: Throwable) {
                authSignInterface.onPostAuthSignInFailed(t.message?:"통신 오류")
            }

        })
    }
}