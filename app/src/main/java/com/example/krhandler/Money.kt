package com.example.krhandler

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

enum class MoneyType {
    GAIN,
    LOSS,
    HOLD,
}

class Money (_id: String, _createdAt: Date, _updatedAt: Date, _description : String, _amount : Double, _type : MoneyType, _currentMoney : Double, _isError : Boolean) :
    BaseData(_id, _createdAt, _updatedAt){

    private var description =  _description
    private var amount = _amount
    private var type = _type
    private var total : Double? = null
    private var isError = _isError
    private val currentMoney = _currentMoney

    fun isError() = isError
    fun setTotal(_total : Double){
        total = _total
    }
    fun getDescription() = description
    fun setDescription(_description: String){
        description = _description
    }
    fun getTotal() = total
    fun getAmount() = amount
    fun getCurrentMoney() = currentMoney
    fun setAmount(_amount : Double){
        amount = _amount
    }

    fun getAmountToAdd() : Double {
        if (total != null)
            return total as Double
        if (type == MoneyType.LOSS)
            return -amount
        return amount
    }

    fun getType() = type
    fun setAmount(_type : MoneyType){
        type = _type
    }
}

class MoneyHandler(){

    fun moneyToHashmap(_amount : Double, _description: String ,_type : MoneyType, _createdAt : Date?): HashMap<String, Any> {
        val newEntry: HashMap<String, Any> = HashMap()
        val cal = Calendar.getInstance()
        newEntry["amount"] = _amount
        newEntry["created_at"] = _createdAt ?: cal.time
        newEntry["updated_at"] = cal.time
        newEntry["description"] = _description
        newEntry["type"] = _type
        return newEntry
    }

    fun toMoney(data : QueryDocumentSnapshot, currentMoney: Double?) : Money {
        val id = data.id
        val amount = data.getDouble("amount")
        val createdAt = data.getDate("created_at")
        val updatedAt = data.getDate("updated_at")
        val description = data.getString("description")
        var type = data.getString("type")
        val moneyType = parseStringToMoneyType(type)

        var newCurrentMoney = amount

        if (createdAt != null && updatedAt != null && amount != null && description != null && newCurrentMoney != null){
            if(currentMoney != null){
                if(moneyType == MoneyType.GAIN){
                    newCurrentMoney = currentMoney + amount
                }
                if(moneyType == MoneyType.LOSS){
                    newCurrentMoney = currentMoney - amount
                }
            }
            return Money(id, createdAt, updatedAt, description, amount, moneyType, newCurrentMoney ,false)
        }

        val cal = Calendar.getInstance()
        return Money(id, cal.time, cal.time, "",0.0, moneyType, 0.0, false)

    }

    fun newMoneyId() : String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DATE)
        return "$year-$month-$day-${cal.timeInMillis}"
    }

    private fun parseStringToMoneyType(str : String?) : MoneyType{
        return try {
            if (str != null) MoneyType.valueOf(str.uppercase())
            else MoneyType.LOSS
        } catch (e : Exception) {
            MoneyType.LOSS
        }
    }

    fun indexOfMoneyType(_type : MoneyType?) : Int {
        val moneyTypeList = MoneyType.values().map { it.toString() }
        return moneyTypeList.indexOf(_type.toString())
    }
}

class MoneyAdapter(private val context: Activity, private val objects: MutableList<Money>) :
    ArrayAdapter<Money>(context, R.layout.view_money, objects) {

    @SuppressLint("MissingInflatedId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView=inflater.inflate(R.layout.view_money,null,true)

        val tvAmount = rowView.findViewById<TextView>(R.id.tvMoneyAmount)
        val tvUpdatedAt = rowView.findViewById<TextView>(R.id.tvMoneyUpdatedAt)
        val tvDescription = rowView.findViewById<TextView>(R.id.tvMoneyDescription)
        val tvTotal = rowView.findViewById<TextView>(R.id.tvMoneyTotal)

        var currObj = objects[position]

        tvTotal.text = currObj.getCurrentMoney().toString()
        tvDescription.text = currObj.getDescription()
        tvAmount.text = currObj.getAmount().toString()
        tvAmount.setTextColor(if (currObj.getType() === MoneyType.LOSS) Color.RED  else Color.GREEN)
        tvUpdatedAt.text = SimpleDateFormat("dd/MM/yyyy").format(currObj.getUpdatedAt())

        return rowView
    }
}