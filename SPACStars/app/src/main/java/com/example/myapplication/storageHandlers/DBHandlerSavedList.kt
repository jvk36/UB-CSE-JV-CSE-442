package com.example.myapplication.storageHandlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//Source: https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial
//https://www.tutorialspoint.com/how-to-use-a-simple-sqlite-database-in-kotlin-android

class DBHandlerSavedList(context: Context) : DBHandlerBase(context) {

    //methods for the saved list
    //inserts SPAC into saved list
    fun insertNewSavedSPAC(ticker: String, name: String, category: String){
        if(!getSavedSPACExists(ticker)){
            val values = ContentValues()
            values.put("ticker", ticker)
            values.put("name", name)
            values.put("category", category)
            val db = this.writableDatabase

            while(db.isDbLockedByCurrentThread){
                println(db.isDbLockedByCurrentThread)
            }

            db.insert("SavedList", null, values)
            db.close()
        }
    }


    //gets all SPACs from saved list
    fun getAllSavedSPAC(): MutableList<Array<String>>{
//        val query = "SELECT * FROM SavedList"
        val db = this.writableDatabase
//        val result = db.rawQuery(query, null)

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        val result = db.query(false, "SavedList",null,null, null, null, null, null, null)

        val finalList: MutableList<Array<String>> = mutableListOf()
        if (result.moveToFirst()) {
            do {
                finalList.add(arrayOf(result.getString(1), result.getString(2)))
            }
            while (result.moveToNext())
//            result.close()
        }

        db.close()
        return finalList
    }

    //check if a SPAC was saved
    fun getSavedSPACExists(ticker: String): Boolean{
        var exists = false
//        val query = "SELECT * FROM SavedList WHERE ticker = \"$ticker\""
        val db = this.writableDatabase
//        val result = db.rawQuery(query, null)

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        val result = db.query(false, "SavedList", null, "ticker=?", arrayOf(ticker), null, null, null, null)

        if (result.moveToFirst()) {
            exists = true
//            result.close()
        }
        result.close()
        db.close()
        return exists
    }

    fun getSavedSPACInfo(ticker: String): MutableList<String>{
        var info = mutableListOf<String>()
//        val query = "SELECT * FROM SavedList WHERE ticker = \"$ticker\""
        val db = this.writableDatabase
//        val result = db.rawQuery(query, null)

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        val result = db.query(false, "SavedList", null, "ticker=?", arrayOf(ticker), null, null, null, null)
        if (result.moveToFirst()) {
            info = mutableListOf(result.getString(1), result.getString(2), result.getString(3))
            result.close()
        }
        result.close()
        return info
    }


    //remove SPAC from saved list
    fun removeSPAC(ticker: String){
        val db = this.writableDatabase


        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        val result = db.query(false, "SavedList", null, "ticker=?", arrayOf(ticker), null, null, null, null)

        if (result.moveToFirst()) {
            db.delete("SavedList", "ticker = ?", arrayOf(ticker))
            result.close()
        }
        db.close()
    }

//    override fun rebuildTable() {
//
//    }
//
//    override fun createTable() {
//    }


}