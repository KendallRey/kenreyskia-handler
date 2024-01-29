package com.example.krhandler

import java.util.Date

abstract class BaseData (_id : String, _createAt : Date, _updatedAt : Date){

    private val id : String = _id
    private val createdAt : Date = _createAt
    private var updatedAt : Date = _updatedAt

    fun getId() : String = id
    fun getCreatedAt() : Date = createdAt
    fun getUpdatedAt() : Date = updatedAt

    fun setUpdatedAt(_updatedAt: Date){
        updatedAt = _updatedAt
    }
}