package com.example.krhandler

import com.google.firebase.firestore.DocumentSnapshot

class UserHandler {

    fun dataToAdmin(_id : String, data : DocumentSnapshot) : AdminUser?{
        val fName = data.getString("first_name")
        val lName = data.getString("last_name")

        if (fName !== null && lName !== null) {
            return AdminUser(_id, fName, lName)
        }
        return null
    }
}