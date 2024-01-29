package com.example.krhandler

abstract class BaseUser(_id : String, _firstName : String, _lastName : String) : java.io.Serializable {

    private val id : String = _id
    private var firstName : String = _firstName
    private var lastName : String = _lastName

    fun getId(): String = id
    fun getFirstName(): String = firstName
    fun getLastName(): String = lastName

    fun setFirstName(firstName: String) {
        this.firstName = firstName
    }
    fun setLastName(lastName: String){
        this.lastName = lastName
    }
}
