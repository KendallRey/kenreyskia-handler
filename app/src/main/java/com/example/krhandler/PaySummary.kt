package com.example.krhandler

import java.util.*

class PaySummary(_id: String, _createdAt: Date, _updatedAt: Date, _amount: Double, _coins: Double, _month : Months?, _isError : Boolean) :
    BaseData(_id, _createdAt, _updatedAt) {

    private var amount : Double = _amount
    private var coins : Double = _coins
    private var total : Double = amount-coins
    private var error : Boolean = _isError
    private var month : Months? = _month

    fun getAmount() = amount
    fun getCoins() = coins
    fun getTotal() = total
    fun getMonth() = month
    fun isError() = error

    fun setMonth(_month : Months){
        month = _month
    }

    fun setAmount(_amount : Double) {
        amount = _amount
        total = _amount - coins
    }

    fun setCoins(_coins : Double) {
        coins = _coins
        total = amount - _coins
    }
}