package com.shall_we.admin.retrofit

import android.content.Context
import android.util.Log
import com.shall_we.admin.App
import com.shall_we.admin.App.Companion.context
import com.shall_we.admin.login.data.RefreshTokenReq
import com.shall_we.admin.login.retrofit.TokenRefreshService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 레트로핏 클라이언트 선언
    var retrofitClient : Retrofit? = null

    // 레트로핏 클라이언트 가져오기
    fun getClient(baseUrl : String): Retrofit? {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 요청 및 응답 바디를 포함한 모든 정보를 로그로 출력
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(TokenInterceptor())
            .build()

        retrofitClient = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            //.client(client.build())
            .build()

        return retrofitClient
    }

    fun getClient2(baseUrl : String): Retrofit? {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 요청 및 응답 바디를 포함한 모든 정보를 로그로 출력
        }
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

        retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                //.client(client.build())
                .build()


        return retrofitClient
    }
}

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val accessToken: String? = App.accessToken
            val request = if (accessToken != null) {
                chain.request().newBuilder().header(AUTHORIZATION, "Bearer $accessToken").build()
            } else {
                chain.request()
            }

            val response = chain.proceed(request)

            // 응답 코드가 401 (Unauthorized)인지 확인
            if (response.code == 401) {
                // 토큰 새로고침 시도
                val refreshedAccessToken = refreshToken()


                // 토큰 새로고침이 성공하면 새 토큰으로 원래의 요청을 다시 시도
                if (!refreshedAccessToken.isNullOrBlank()) {
                    val sharedPref = context?.getSharedPreferences("com.shall_we", Context.MODE_PRIVATE)
                    val accessToken = refreshedAccessToken
                    sharedPref?.edit()?.putString("access_token", accessToken)?.apply()

                    App.accessToken = sharedPref?.getString("access_token", null)

                    Log.d("login","access token ${App.accessToken}")

                    val newRequest = request.newBuilder().header(AUTHORIZATION, "Bearer $refreshedAccessToken").build()
                    return@runBlocking chain.proceed(newRequest)
                }
            }

            // 토큰 새로고침이 필요하지 않거나 실패한 경우 원래의 응답을 반환
            response
        }
    }

    private suspend fun refreshToken(): String? {
        // 토큰 새로고침 로직을 여기에 추가하십시오. AuthAuthenticator 클래스 또는 다른 방법을 사용할 수 있습니다.
        // 새로 고친 액세스 토큰을 반환하거나 새로 고침이 실패하면 null을 반환
        val refreshTokenArray = RefreshTokenReq(App.refreshToken!!)
        val result = TokenRefreshService.instance.tokenRefresh(refreshTokenArray)
        if (result?.isSuccessful == true) {
            val authResponse = result.body()
            return authResponse?.data?.accessToken
        }
        return null
    }

    companion object {
        private const val AUTHORIZATION = "authorization"
    }
}
