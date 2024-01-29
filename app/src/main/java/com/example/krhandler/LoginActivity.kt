package com.example.krhandler

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private var btnLogin : Button? = null
    private var etEmail : EditText? = null
    private var etPassword : EditText? = null
    private var cbRememberMe : CheckBox? = null

    private var auth: FirebaseAuth = Firebase.auth

    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance();
    private val collectionRef : CollectionReference = firestore.collection("Admin")
    private var documentRef : DocumentReference? = null

    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setViews()
        setEventHandlers()
        handlePreferences()
    }

    private fun setViews() {
        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        cbRememberMe = findViewById(R.id.cbLoginRememberMe)
    }

    private fun setEventHandlers() {
        btnLogin?.setOnClickListener {
            if(utils.isOnline(this)){
                startAuth()
                return@setOnClickListener
            }
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(editText : EditText) : String?{
        val value = editText.text.toString()
        if (value.trim() === "")
            return null
        return value
    }

    private var isLogging = false
    private fun isLoggingStarted(){
        isLogging = true
        btnLogin?.setText(R.string.btnProcessing)
        btnLogin?.isEnabled = false
    }
    private fun isLoggingFailed(){
        isLoggingDone()
    }
    private fun isLoggingDone(){
        isLogging = false
        btnLogin?.setText(R.string.btnLogin)
        btnLogin?.isEnabled = true
    }

    private fun startAuth(){
        if (isLogging) return
        val email : String? = etEmail?.let { validateInput(it) }
        val password : String? = etPassword?.let { validateInput(it) }

        if (email.isNullOrEmpty() || email.isBlank()){
            Toast.makeText(this, "Email can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isNullOrEmpty() || password.isBlank()){
            Toast.makeText(this, "Password can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        isLoggingStarted()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    getProfile(email, password)
                    return@addOnCompleteListener
                }
//                Toast.makeText(this, "ERROR : LA-SA-TIF-001", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Wrong Credentials!", Toast.LENGTH_SHORT).show()
                isLoggingFailed()
                return@addOnCompleteListener
            }
    }


    private  val handler = UserHandler();
    private var admin : AdminUser? = null

    private fun getProfile(email : String, password : String){
        if(auth.currentUser == null){
            Toast.makeText(this, "ERROR : LA-GP-CIN-001", Toast.LENGTH_SHORT).show()
            isLoggingFailed()
            return
        }

        val id = auth.currentUser!!.uid
        documentRef = collectionRef.document(id)
        documentRef!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful){
                admin = handler.dataToAdmin(id, task.result)
                if(admin !== null){
                    savePreferences(email, password)
                    startMainActivity(admin!!)
                    return@addOnCompleteListener
                }
                Toast.makeText(this, "ERROR : LA-GP-AIN-001", Toast.LENGTH_SHORT).show()
                isLoggingFailed()
                return@addOnCompleteListener
            }
            Toast.makeText(this, "ERROR : LA-GP-TIF-001", Toast.LENGTH_SHORT).show()
            isLoggingFailed()
            return@addOnCompleteListener
        }
    }

    private fun startMainActivity(admin : AdminUser){
//        val intent = Intent(this, MainActivity::class.java)
        val intent = Intent(this, MoneyActivity::class.java)
        intent.putExtra("admin",admin)
        startActivity(intent)
        finish()
    }

    private var sharedPreferences :SharedPreferences? = null

    private fun savePreferences(email : String, password : String){
        val doRememberMe = cbRememberMe?.isChecked
        val editor = sharedPreferences?.edit()

        if(doRememberMe !== null && editor !== null && doRememberMe){
            editor.putBoolean("doRememberMe",true)
            editor.putString("userEmail",email)
            editor.putString("userPassword",password)
            editor.commit()
            return
        }
        editor?.remove("doRememberMe")
        editor?.remove("userEmail")
        editor?.remove("userPassword")
        editor?.commit()
        return
    }

    private fun handlePreferences(){
        sharedPreferences = getPreferences(MODE_PRIVATE)
        val doRemember = getBooleanPreferences("doRememberMe")

        if ((doRemember !== null) && doRemember){
            cbRememberMe?.isChecked = true
            etEmail?.setText(getStringPreferences("userEmail"))
            etPassword?.setText(getStringPreferences("userPassword"))
        }
    }

    private fun getBooleanPreferences(key : String) : Boolean? = sharedPreferences?.getBoolean(key, false)
    private fun getStringPreferences(key: String) : String? {
        val value = sharedPreferences?.getString(key, "")
        if (value !== null){
            return value
        }
        return ""
    }
}