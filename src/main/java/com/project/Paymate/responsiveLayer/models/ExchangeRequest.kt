package com.project.Paymate.responsiveLayer.models

data class ExchangeRequest(
    val id: Int,
    val requesterId: String,
    val receiverId: String,
    val amount: String,
    val currencyFrom: String,
    val currencyTo: String,
    val status: String,
    val timestamp: String,
    val phoneNumber: String,
    val note: String? = null,
    val meetLocation: String? = null,
    val meetTime: String? = null,
    val upiId: String? = null,
    val isUpiPayment: String
)