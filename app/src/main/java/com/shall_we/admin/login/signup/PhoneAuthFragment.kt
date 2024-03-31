package com.shall_we.admin.login.signup

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shall_we.admin.R
import com.shall_we.admin.databinding.FragmentPhoneAuthBinding
import com.shall_we.admin.login.data.SendOneReq
import com.shall_we.admin.login.data.ValidCodeReq
import com.shall_we.admin.login.retrofit.PhoneAuthService
import com.shall_we.admin.login.retrofit.ValidCodeService
import com.shall_we.admin.retrofit.RESPONSE_STATE


class PhoneAuthFragment : Fragment() {
    private lateinit var timerTv: TextView

    private var timer: CountDownTimer? = null

    private lateinit var phoneNumber : String
    private lateinit var verificationCode : String

    private lateinit var password : String
    private lateinit var confirmPw : String

    var nameFlag: Boolean = false

    private lateinit var nameTxt : String
    private lateinit var phoneNumberTxt : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentPhoneAuthBinding.inflate(inflater,container, false)
        timerTv = binding.timer

        var maxLength = 6
        var filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        binding.codeEt.filters = filters

        maxLength = 11
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        binding.phonenumberEt.filters = filters

        binding.nameEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                // 문자열이 변경될 때마다 호출되는 메소드
                val length = s?.length ?: 0
                if (length>0){
                    nameFlag = true
                }
                else if (length==0){
                    nameFlag = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.authSend.setOnClickListener {
            phoneNumber = binding.phonenumberEt.text.toString()
            if(phoneNumber.length == 11 && nameFlag){
                binding.nameEt.isEnabled = false
                binding.phonenumberEt.isEnabled = false

                nameTxt = binding.nameEt.text.toString()
                phoneNumberTxt = binding.phonenumberEt.text.toString()

                sendRetrofitCall()
                timerTv.visibility = View.VISIBLE
                startTimer()
            }else if(!nameFlag){
                Toast.makeText(context,"이름을 다시 입력해주세요.", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(context,"번호를 다시 입력해주세요.", Toast.LENGTH_LONG).show()
            }

        }

        binding.passwordEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 문자열이 변경될 때마다 호출되는 메소드
                val length = p0?.length ?: 0
                if (length>=8){
                    confirmPw = p0?.toString()!!
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
        binding.confirmpwEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() == confirmPw) {
                    binding.btnNextAuth.setBackgroundResource(R.drawable.btn_next)
                    binding.btnNextAuth.isClickable = true
                }
                else {
                    binding.btnNextAuth.setBackgroundResource(R.drawable.btn_next_black)
                    binding.btnNextAuth.isClickable = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.btnNextAuth.setOnClickListener {
            password = binding.passwordEt.text.toString()
            // 인증번호 검증 -> 번호 맞을때만 다음 프래그먼트로 넘기기
            verificationCode = binding.codeEt.text.toString()
            validRetrofitCall()

            val newFragment = ShopInfoFragment() // 전환할 다른 프래그먼트 객체 생성
            val bundle = Bundle()
            bundle.putString("name",nameTxt)
            bundle.putString("phone",phoneNumber)
            bundle.putString("password",password)
            newFragment.arguments = bundle
            // 프래그먼트 전환
            parentFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView3, newFragment)
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }


    private fun startTimer() {
        timer?.cancel() // 이미 실행 중인 타이머가 있으면 취소합니다.

        timer = object : CountDownTimer(3 * 60 * 1000L, 1000) { // 3분 타이머 생성 (3분 * 60초 * 1000밀리초)
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / (60 * 1000)
                val seconds = (millisUntilFinished % (60 * 1000)) / 1000
                timerTv.text = String.format("%02d:%02d", minutes, seconds) // 03:00 형식으로 표시합니다.
            }

            override fun onFinish() {
                timerTv.text = "00:00" // 타이머가 끝나면 00:00으로 표시합니다.
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // 액티비티가 종료될 때 타이머를 취소합니다.
    }

    private fun sendRetrofitCall() {
        Log.d("retrofit","$phoneNumber")
        val sendOneArray : SendOneReq = SendOneReq(phoneNumber)
        PhoneAuthService().sendOne(sendOneArray, completion = {
                responseState, responseBody ->
            when(responseState){
                RESPONSE_STATE.OKAY -> {
                    Log.d("retrofit", "category api : ${responseBody}")
                }
                RESPONSE_STATE.FAIL -> {
                    Log.d("retrofit", "api 호출 에러")
                }
            }
        })
    }

    private fun validRetrofitCall() {
        Log.d("retrofit","$phoneNumber")
        Log.d("retrofit","$verificationCode")

        val validVerificationArray : ValidCodeReq = ValidCodeReq(verificationCode,phoneNumber)

        ValidCodeService().validVerification(validVerificationArray = validVerificationArray, completion = {
                responseState, responseBody ->
            when(responseState){
                RESPONSE_STATE.OKAY -> {
                    Log.d("retrofit", "category api : ${responseBody.hashCode()}")
                    if(responseBody.hashCode() == 200){
                        val newFragment = ShopInfoFragment() // 전환할 다른 프래그먼트 객체 생성
                        val bundle = Bundle()
                        bundle.putString("name",nameTxt)
                        bundle.putString("phone",phoneNumber)
                        bundle.putString("password",password)
                        newFragment.arguments = bundle
                        // 프래그먼트 전환
                        parentFragmentManager.beginTransaction()
                            .add(R.id.fragmentContainerView3, newFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    else if (responseBody == 400){
                        Toast.makeText(context,"인증번호를 확인해주세요.",Toast.LENGTH_LONG).show()
                    }
                }
                RESPONSE_STATE.FAIL -> {
                    Log.d("retrofit", "api 호출 에러")
                }
            }
        })
    }

}