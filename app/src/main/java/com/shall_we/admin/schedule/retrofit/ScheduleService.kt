import android.util.Log
import com.shall_we.admin.retrofit.API
import com.shall_we.admin.retrofit.IRetrofit
import com.shall_we.admin.retrofit.RESPONSE_STATE
import com.shall_we.admin.retrofit.RetrofitClient
import com.shall_we.admin.schedule.data.ScheduleData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleService {
    private val iRetrofit: IRetrofit? =
        RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun getGift(completion: (RESPONSE_STATE, List<ScheduleData>?) -> Unit) {
        val call = iRetrofit?.getGift()
        call?.enqueue(object : Callback<List<ScheduleData>> {
            override fun onResponse(call: Call<List<ScheduleData>>, response: Response<List<ScheduleData>>) {
                if (response.isSuccessful) {
                    Log.d("check",response.body().toString())
                    val giftList = response.body()
                    completion(RESPONSE_STATE.OKAY, giftList)
                } else {
                    completion(RESPONSE_STATE.FAIL, null)
                }
            }

            override fun onFailure(call: Call<List<ScheduleData>>, t: Throwable) {
                completion(RESPONSE_STATE.FAIL, null)
            }
        })
    }
}