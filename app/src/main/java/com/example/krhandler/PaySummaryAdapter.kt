package com.example.krhandler

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat

class PaySummaryAdapter(private val context: Activity, private val objects: MutableList<PaySummary>) :
    ArrayAdapter<PaySummary>(context, R.layout.view_paysummary, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView=inflater.inflate(R.layout.view_paysummary,null,true)

        val tvAmount = rowView.findViewById<TextView>(R.id.tvAmount)
        val tvCoins = rowView.findViewById<TextView>(R.id.tvCoins)
        val tvTotal = rowView.findViewById<TextView>(R.id.tvTotal)
        val tvUpdatedAt = rowView.findViewById<TextView>(R.id.tvUpdatedAt)

        tvAmount.text = objects[position].getAmount().toString()
        tvCoins.text = objects[position].getCoins().toString()
        tvTotal.text = objects[position].getTotal().toString()
        tvUpdatedAt.text = SimpleDateFormat("dd/MM/yyyy").format(objects[position].getUpdatedAt())

        return rowView
    }
}


val monthList = Months.values().map { it.toString() }
class MonthAdapter(context: Activity) :
    ArrayAdapter<String>(context, R.layout.text_month, monthList)
