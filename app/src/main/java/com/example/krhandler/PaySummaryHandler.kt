package com.example.krhandler

import android.util.Log
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PaySummaryHandler {

    private val monthDate = SimpleDateFormat("MMMM")

    fun paySummaryToHashmap(_amount : Double, _coins : Double, _month : Months, _createdAt : Date?): HashMap<String, Any> {
        val newEntry: HashMap<String, Any> = HashMap()
        val cal = Calendar.getInstance()
        newEntry["amount"] = _amount
        newEntry["coins"] = _coins
        newEntry["created_at"] = _createdAt ?: cal.time
        newEntry["updated_at"] = cal.time
        newEntry["month"] = _month
        return newEntry
    }

    fun toPaySummary(data : QueryDocumentSnapshot) : PaySummary{
        val id = data.id
        val amount = data.getDouble("amount")
        val coins = data.getDouble("coins")
        val createdAt = data.getDate("created_at")
        val updatedAt = data.getDate("updated_at")
        var month = data.getString("month")
        var newMonth : Months? = parseStringToMonth(month)
        if (createdAt != null && updatedAt != null && amount != null && coins !=null ){
            return PaySummary(id, createdAt, updatedAt, amount, coins, newMonth, false)
        }
        val cal = Calendar.getInstance()
        return PaySummary(id, cal.time, cal.time, 0.0, 0.0, null, true)
    }

    fun newPaySummaryId() : String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DATE)
        return "$year-$month-$day"
    }

    private fun parseStringToMonth(str : String?) : Months? {
        return try {
            if (str != null) Months.valueOf(str.uppercase())
            else null
        } catch (e : Exception) {
            null
        }
    }

    fun indexOfMonth(_month : Months?) : Int {
        val monthList = Months.values().map { it.toString() }
        return monthList.indexOf(_month.toString())
    }
}