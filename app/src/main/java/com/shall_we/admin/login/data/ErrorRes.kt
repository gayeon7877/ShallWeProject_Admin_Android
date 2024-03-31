package com.shall_we.admin.login.data

import com.google.gson.annotations.SerializedName

data class ErrorRes(
    @SerializedName("data")
    val data: ErrorData,
    @SerializedName("transaction_time")
    val transactionTime: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("statusCode")
    val statusCode: Int
)

data class ErrorData(
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("class")
    val className: String?,
    @SerializedName("errors")
    val errors: List<String>
)
