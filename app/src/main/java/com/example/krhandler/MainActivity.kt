package com.example.krhandler

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val paySummary : MutableList<PaySummary> = mutableListOf()

    private var adminUser : AdminUser? = null

    private var srlMainActivity : SwipeRefreshLayout? = null
    private var clAddPaySummary : ConstraintLayout? = null
    private var lvPaySummary : ListView? = null
    private var arrayAdapter: ArrayAdapter<PaySummary>? = null


    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance();
    private val collectionRef : CollectionReference = firestore.collection("PaySummary")
    private var documentRef : DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAdmin()

        setViews()
        setAdapters()
        setEventHandlers()

        getPaySummaries()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.main_money -> {
                val intent = Intent(this, MoneyActivity::class.java)
                intent.putExtra("admin",adminUser)
                startActivity(intent)
                finish()
            }
            R.id.main_addPaySummary -> {
                viewAddingPaySummary()
            }
            R.id.main_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAdmin(){
        adminUser = intent.getSerializableExtra("admin") as AdminUser
    }

    private fun setViews(){
        srlMainActivity = findViewById(R.id.srlMain)
        lvPaySummary = findViewById(R.id.lvPaySummary)
        clAddPaySummary = findViewById(R.id.clAddPaySummary)

        btnAddPaySummary = findViewById(R.id.btnAddPaySummary)
        btnCancelPaySummary = findViewById(R.id.btnCancelPaySummary)
        btnDeletePaySummary = findViewById(R.id.btnDeletePaySummary)

        tvAddPaySummaryAmountLabel = findViewById(R.id.tvAddPaySummaryAmountLabel)
        tvAddPaySummaryCoinsLabel = findViewById(R.id.tvAddPaySummaryCoinsLabel)
        tvAddPaySummaryTotalLabel = findViewById(R.id.tvAddPaySummaryTotalLabel)
        tvAddPaySummaryMonthLabel = findViewById(R.id.tvAddPaySummaryMonthLabel)

        etAddPaySummaryAmount = findViewById(R.id.etAddPaySummaryAmount)
        etAddPaySummaryCoins = findViewById(R.id.etAddPaySummaryCoins)
        etAddPaySummaryTotal = findViewById(R.id.etAddPaySummaryTotal)
        sAddPaySummaryMonth = findViewById(R.id.sAddPaySummaryMonth)
    }

    private var selectedPaySummary : PaySummary? = null

    private fun setEventHandlers(){
        srlMainActivity?.setOnRefreshListener {
            getPaySummaries()
            srlMainActivity?.isRefreshing = false
        }

        lvPaySummary?.onItemClickListener = AdapterView.OnItemClickListener {
            adapterView, view, i, l ->
            selectedPaySummary = adapterView.getItemAtPosition(i) as PaySummary
            if (selectedPaySummary!=null)
                setSelectedPaySummary(selectedPaySummary!!)
            else
                Toast.makeText(this,"MA-SEH-SPSIN-001", Toast.LENGTH_SHORT).show()
        }

        btnAddPaySummary?.setOnClickListener {startAddingPaySummary()}
        btnCancelPaySummary?.setOnClickListener {stopAddingPaySummary()}
        btnDeletePaySummary?.setOnClickListener {startDeletingPaySummary()}
    }

    private fun setSelectedPaySummary(_paySummary : PaySummary){
        btnAddPaySummary?.text = "UPDATE"
        etAddPaySummaryAmount?.setText(_paySummary.getAmount().toString())
        etAddPaySummaryCoins?.setText(_paySummary.getCoins().toString())
        etAddPaySummaryTotal?.setText(_paySummary.getTotal().toString())
        sAddPaySummaryMonth?.setSelection(psHandler.indexOfMonth(_paySummary.getMonth()))
        btnDeletePaySummary?.visibility=View.VISIBLE
        viewAddingPaySummary()
    }

    private fun setAdapters(){
        arrayAdapter = PaySummaryAdapter(this, paySummary)
        lvPaySummary?.adapter = arrayAdapter
        sAddPaySummaryMonth?.adapter = MonthAdapter(this)
    }

    private val psHandler = PaySummaryHandler()
    private fun getPaySummaries(){

        srlMainActivity?.isRefreshing = true

        collectionRef
            .orderBy("created_at")
            .get()
            .addOnCompleteListener { task ->
                srlMainActivity?.isRefreshing = false
                if(task.isSuccessful){
                    paySummary.clear()
                    for (data in task.result){
                        val temp = psHandler.toPaySummary(data)
                        paySummary += temp
                    }
                    paySummary.reverse()
                    arrayAdapter?.notifyDataSetChanged()
                }
            }
    }

    private var tvAddPaySummaryAmountLabel : TextView? = null
    private var tvAddPaySummaryCoinsLabel : TextView? = null
    private var tvAddPaySummaryTotalLabel : TextView? = null
    private var tvAddPaySummaryMonthLabel : TextView? = null

    private var etAddPaySummaryAmount : EditText? = null
    private var etAddPaySummaryCoins : EditText? = null
    private var etAddPaySummaryTotal : EditText? = null
    private var sAddPaySummaryMonth : Spinner? = null

    private var btnAddPaySummary : Button? = null
    private var btnCancelPaySummary : Button? = null
    private var btnDeletePaySummary : Button? = null

    private fun viewAddingPaySummary(){
        clAddPaySummary?.visibility = View.VISIBLE
        srlMainActivity?.visibility = View.GONE
    }

    @SuppressLint("ResourceAsColor")
    private fun startAddingPaySummary(){
        if(btnAddPaySummary?.isEnabled == false){
            return
        }
        val amount = etAddPaySummaryAmount?.text?.toString()?.toDoubleOrNull()
        val coins = etAddPaySummaryCoins?.text?.toString()?.toDoubleOrNull()

        if ((amount == null) || amount.isNaN()){
            tvAddPaySummaryAmountLabel?.setTextColor(ContextCompat.getColor(this,R.color.danger))
            return
        }
        if ((coins == null) || coins.isNaN() || amount<coins){
            tvAddPaySummaryCoinsLabel?.setTextColor(ContextCompat.getColor(this,R.color.danger))
            return
        }
        tvAddPaySummaryAmountLabel?.setTextColor(ContextCompat.getColor(this,R.color.black))
        tvAddPaySummaryCoinsLabel?.setTextColor(ContextCompat.getColor(this,R.color.black))

        val month = sAddPaySummaryMonth?.selectedItem.toString()

        var entry: HashMap<String, Any>?
        if (selectedPaySummary == null){
            documentRef = collectionRef.document(psHandler.newPaySummaryId())
            entry = psHandler.paySummaryToHashmap(amount,coins, Months.valueOf(month), null)
        }
        else{
            documentRef = collectionRef.document(selectedPaySummary!!.getId())
            entry = psHandler.paySummaryToHashmap(amount,coins, Months.valueOf(month), selectedPaySummary!!.getCreatedAt())
        }

        setButtonEnabled(false)

        documentRef?.set(entry)?.addOnCompleteListener { task ->
            if (task.isSuccessful){

                if (selectedPaySummary!=null)
                    Toast.makeText(this, "Pay Summary Updated!", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Pay Summary Added!", Toast.LENGTH_SHORT).show()

                getPaySummaries()
                stopAddingPaySummary()
                return@addOnCompleteListener
            }
            Toast.makeText(this, "Adding Pay Summary Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAddingPaySummary(){
        selectedPaySummary = null
        btnAddPaySummary?.text = "ADD"
        btnDeletePaySummary?.visibility = View.GONE
        setButtonEnabled(true)
        etAddPaySummaryAmount?.setText("0.0")
        etAddPaySummaryCoins?.setText("0.0")
        etAddPaySummaryTotal?.setText("0.0")
        sAddPaySummaryMonth?.setSelection(0)
        clAddPaySummary?.visibility = View.GONE
        srlMainActivity?.visibility = View.VISIBLE
    }

    private fun setButtonEnabled(_isEnabled : Boolean){
        btnAddPaySummary?.isEnabled = _isEnabled
        btnCancelPaySummary?.isEnabled = _isEnabled
        btnDeletePaySummary?.isEnabled = _isEnabled
    }

    private fun startDeletingPaySummary(){
        if (selectedPaySummary == null){
            Toast.makeText(this, "ERROR : MA-SDPS-SPIN-001", Toast.LENGTH_SHORT).show()
            return
        }
        setButtonEnabled(false)
        documentRef = collectionRef.document(selectedPaySummary!!.getId())
        documentRef?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Pay Summary Deleted!", Toast.LENGTH_SHORT).show()

                getPaySummaries()
                stopAddingPaySummary()
                return@addOnCompleteListener
            }
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()

        }
    }
}