package com.example.krhandler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class MoneyActivity : AppCompatActivity() {

    private val money : MutableList<Money> = mutableListOf()

    private var adminUser : AdminUser? = null

    private var srlMoneyActivity : SwipeRefreshLayout? = null
    private var clAddMoney : ConstraintLayout? = null
    private var lvMoney : ListView? = null
    private var arrayAdapter: ArrayAdapter<Money>? = null


    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance();
    private val collectionRef : CollectionReference = firestore.collection("Money")
    private var documentRef : DocumentReference? = null

    private val moneyHandler = MoneyHandler()

    private var btnAddMoney : Button? = null
    private var btnCancelMoney : Button? = null
    private var btnDeleteMoney : Button? = null

    private var tvAddMoneyAmountLabel : TextView? = null
    private var tvAddMoneyTypeLabel : TextView? = null
    private var tvAddMoneyDescription : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money)

        setAdmin()

        setViews()
        setAdapters()
        setEventHandlers()

        getMoney()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.money_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.money_summary -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("admin",adminUser)
                startActivity(intent)
                finish()
            }
            R.id.money_addMoney -> {
                viewAddingMoney()
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

    private fun viewAddingMoney(){
        clAddMoney?.visibility = View.VISIBLE
        srlMoneyActivity?.visibility = View.GONE
    }

    private fun setAdmin() {
        adminUser = intent.getSerializableExtra("admin") as AdminUser
    }

    private fun setViews() {
        srlMoneyActivity = findViewById(R.id.srlMoney)
        lvMoney = findViewById(R.id.lvMoney)
        clAddMoney = findViewById(R.id.clAddMoney)

        btnAddMoney = findViewById(R.id.btnAddMoney)
        btnCancelMoney = findViewById(R.id.btnCancelMoney)
        btnDeleteMoney = findViewById(R.id.btnDeleteMoney)

        tvAddMoneyAmountLabel = findViewById(R.id.tvAddMoneyAmountLabel)
        tvAddMoneyTypeLabel = findViewById(R.id.tvAddMoneyTypeLabel)
        tvAddMoneyDescription = findViewById(R.id.tvAddMoneyDescriptionLabel)

        etAddMoneyAmount = findViewById(R.id.etAddMoneyAmount)
        etAddMoneyDescription = findViewById(R.id.etAddMoneyDescription)
        sAddMoneyType = findViewById(R.id.sAddMoneyType)
    }

    private fun setAdapters() {
        arrayAdapter = MoneyAdapter(this, money)
        lvMoney?.adapter = arrayAdapter
    }

    private var selectedMoney : Money? = null

    private fun setEventHandlers() {

        srlMoneyActivity?.setOnRefreshListener {
            getMoney()
            srlMoneyActivity?.isRefreshing = false
        }

        lvMoney?.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->
            selectedMoney = adapterView.getItemAtPosition(i) as Money
            if (selectedMoney!=null)
                setSelectedMoney(selectedMoney!!)
            else
                Toast.makeText(this,"MoA-SEH-SMIN-001", Toast.LENGTH_SHORT).show()
        }

        btnAddMoney?.setOnClickListener {startAddingMoney()}
        btnCancelMoney?.setOnClickListener {stopAddingMoney()}
        btnDeleteMoney?.setOnClickListener {startDeletingMoney()}
    }

    private fun startDeletingMoney() {
        if (selectedMoney == null){
            Toast.makeText(this, "ERROR : MA-SDM-SMIN-001", Toast.LENGTH_SHORT).show()
            return
        }
        setButtonEnabled(false)
        documentRef = collectionRef.document(selectedMoney!!.getId())
        documentRef?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Money Deleted!", Toast.LENGTH_SHORT).show()

                getMoney()
                stopAddingMoney()
                return@addOnCompleteListener
            }
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun stopAddingMoney() {
        clAddMoney?.visibility = View.GONE
        srlMoneyActivity?.visibility = View.VISIBLE
        setButtonEnabled(true)
        btnAddMoney?.text = "ADD"
    }

    private var etAddMoneyAmount : EditText? = null
    private var etAddMoneyDescription : EditText? = null
    private var sAddMoneyType : Spinner? = null

    private fun startAddingMoney() {
        if(btnAddMoney?.isEnabled == false){
            return
        }

        val amount = etAddMoneyAmount?.text?.toString()?.toDoubleOrNull()
        val description = etAddMoneyDescription?.text?.toString()
        val type = MoneyType.valueOf(sAddMoneyType?.selectedItem.toString())

        if ((amount == null) || amount.isNaN() || amount<0){
            tvAddMoneyAmountLabel?.setTextColor(ContextCompat.getColor(this,R.color.danger))
            return
        }
        if ((description == null) || description?.trim() == ""){
            tvAddMoneyDescription?.setTextColor(ContextCompat.getColor(this,R.color.danger))
            return
        }

        var entry: HashMap<String, Any>?
        if(selectedMoney == null){
            documentRef = collectionRef.document(moneyHandler.newMoneyId())
            entry = moneyHandler.moneyToHashmap(amount, description, type, null)
        }
        else{
            documentRef = collectionRef.document(selectedMoney!!.getId())
            entry = moneyHandler.moneyToHashmap(amount, description, type, selectedMoney!!.getCreatedAt())
        }

        setButtonEnabled(false)

        documentRef?.set(entry)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                if (selectedMoney!=null)
                    Toast.makeText(this, "Money Updated!", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Money Added!", Toast.LENGTH_SHORT).show()

                getMoney()
                stopAddingMoney()
                return@addOnCompleteListener
            }
            Toast.makeText(this, "Adding Money Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setButtonEnabled(_isEnabled : Boolean){
        btnAddMoney?.isEnabled = _isEnabled
        btnCancelMoney?.isEnabled = _isEnabled
        btnDeleteMoney?.isEnabled = _isEnabled
    }

    private fun setSelectedMoney(_money : Money){
        etAddMoneyAmount?.setText(_money.getAmount().toString())
        etAddMoneyDescription?.setText(_money.getDescription())
        sAddMoneyType?.setSelection(moneyHandler.indexOfMoneyType(_money.getType()))
        btnAddMoney?.text = "UPDATE"
        btnDeleteMoney?.visibility=View.VISIBLE
        viewAddingMoney()
    }

    private fun getMoney(){

        srlMoneyActivity?.isRefreshing = true

        var currentMoney : Double? = null

        collectionRef
            .orderBy("created_at")
            .get()
            .addOnCompleteListener { task ->
                srlMoneyActivity?.isRefreshing = false
                if(task.isSuccessful){
                    money.clear()
                    for (data in task.result){
                        Log.e("id",data.id)
                        val temp = moneyHandler.toMoney(data, currentMoney)
                        currentMoney = temp.getCurrentMoney()
                        money += temp
                    }
                    arrayAdapter?.notifyDataSetChanged()
                }
            }
    }
}