package com.example.myapplication.storageHandlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication.constants

open class DBHandlerBase(context: Context) : SQLiteOpenHelper(context, "SPACStars", null, 3){

    val dbMap: Map<String, String> = mapOf(
        "Pre+LOI" to "PreLOI",
        "Definitive+Agreement" to "DefAgreement",
        "Pre+Unit+Split" to "PreUnitSplit",
        "Pre+IPO" to "PreIPO"
    )

    override fun onCreate(db: SQLiteDatabase?) {
        var create =
                "CREATE TABLE IF NOT EXISTS " +
                        "SavedList " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "category VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreLOI " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "average_volume VARCHAR(256)," +
                        "warrant_ticker VARCHAR(256)," +
                        "target_focus VARCHAR(256)," +
                        "underwriters VARCHAR(256)," +
                        "IPO_date VARCHAR(256)," +
                        "deadline_date VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "DefAgreement " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "volume_average VARCHAR(256)," +
                        "target VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "OptionChads " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "average_volume VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreUnitSplit " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "unit_warrant_details VARCHAR(256)," +
                        "estimated_trust_size VARCHAR(256)," +
                        "prominent_leadership VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreIPO " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "management_team VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
//        db?.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS SavedList")
        db?.execSQL("DROP TABLE IF EXISTS PreLOI")
        db?.execSQL("DROP TABLE IF EXISTS DefAgreement")
        db?.execSQL("DROP TABLE IF EXISTS OptionChads")
        db?.execSQL("DROP TABLE IF EXISTS PreUnitSplit")
        db?.execSQL("DROP TABLE IF EXISTS PreIPO")
        onCreate(db)
    }

    fun insertNewSPAC(ticker: String?, writableDB: SQLiteDatabase, tablename: String?, columns: Int?, data: Map<String, String>){
        println("inserting")
        createAll()
        if(getSPACData(ticker, writableDB, tablename, columns).isEmpty()){
            val values = ContentValues()
            for((k, v) in data){
                values.put(k,v)
            }
            val db = this.writableDatabase

            while(db.isDbLockedByCurrentThread){
                println(db.isDbLockedByCurrentThread)
            }

            db.insert(tablename, null, values)
            db.close()
        }
    }

    fun bulkInsertSPAC(tablename: String?, columns: Int?, data: MutableList<Map<String,String>>){
        println("bulk inserting")
        val db = this.writableDatabase
        createAll()
        db.beginTransaction()
        try{
            val values = ContentValues()
            for(i in data){
                for((k, v) in i){
                    values.put(k,v)
                }
                db.insert(tablename, null, values)
            }
            db.setTransactionSuccessful()
        }finally{
            db.endTransaction()
        }
//        println(db.query(false, tablename, null, null, null, null, null, null, null))
        db.close()
    }

    fun getSPACData(ticker: String?, writableDB: SQLiteDatabase, tablename: String?, columns: Int?): MutableList<String>{
        var info = mutableListOf<String>()
        val db = this.writableDatabase

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }
        createAll()

        val result = db.query(false, tablename, null, "ticker=?", arrayOf(ticker), null, null, null, null)


        if (result.moveToFirst()) {
            for(i in 1 until columns as Int + 1){
                info.add(result.getString(i))
            }
            result.close()
        }
        result.close()
        db.close()
        return info
    }

    fun getAllSPACData(writableDB: SQLiteDatabase, tablename: String?, columns: Int?): MutableList<Array<String>>{
        val db = this.writableDatabase

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        createAll()

        val result = db.query(false, tablename, null, null, null, null, null, null, null)

        val finalList: MutableList<Array<String>> = mutableListOf()
        if (result.moveToFirst()) {
            do {
                val row = mutableListOf<String>()
                for(i in 1 until columns as Int + 1){
                    row.add(result.getString(i))
                }
                val info = row.toTypedArray()
                finalList.add(info)
            }
            while (result.moveToNext())
            result.close()
        }

        db.close()

        return finalList
    }

    fun removeSPAC(ticker: String?, writableDB: SQLiteDatabase, tablename: String?){
        val db = this.writableDatabase
        createAll()

        while(db.isDbLockedByCurrentThread){
            println(db.isDbLockedByCurrentThread)
        }

        val result = db.query(false, "SavedList", null, "ticker=?", arrayOf(ticker), null, null, null, null)

        if (result.moveToFirst()) {
            db.delete(tablename, "ticker = ?", arrayOf(ticker))
            result.close()
        }
        db.close()
    }

//    abstract fun rebuildTable()
//    abstract fun createTable()

    fun createTable(category: String){
        var create = ""
        val db = this.writableDatabase
        println(category)
        when(category){
            "Pre+LOI" -> {
                create =
                    "CREATE TABLE IF NOT EXISTS " +
                            "PreLOI " +
                            "(" +
                            "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "ticker VARCHAR(256)," +
                            "name VARCHAR(256)," +
                            "market_cap VARCHAR(256)," +
                            "estimated_trust_value VARCHAR(256)," +
                            "current_volume VARCHAR(256)," +
                            "average_volume VARCHAR(256)," +
                            "warrant_ticker VARCHAR(256)," +
                            "target_focus VARCHAR(256)," +
                            "underwriters VARCHAR(256)," +
                            "IPO_date VARCHAR(256)," +
                            "deadline_date VARCHAR(256)" +
                            ")"
            }
            "Definitive+Agreement" -> {
                create = "CREATE TABLE IF NOT EXISTS " +
                        "DefAgreement " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "volume_average VARCHAR(256)," +
                        "target VARCHAR(256)" +
                        ")"
            }
            "Pre+Unit+Split" -> {
                create = "CREATE TABLE IF NOT EXISTS " +
                        "PreUnitSplit " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "unit_warrant_details VARCHAR(256)," +
                        "estimated_trust_size VARCHAR(256)," +
                        "prominent_leadership VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"
            }
            "Pre+IPO" -> {
                create = "CREATE TABLE IF NOT EXISTS " +
                        "PreIPO " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "management_team VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"
            }
        }
        println(create)
        db.execSQL(create)

    }

    fun rebuildTable(category: String){
        val db = this.writableDatabase
        val table = dbMap[category]
        println(table)
        db.execSQL("DROP TABLE IF EXISTS $table")
        createTable(category)
    }

    fun createAll(){
        val db = this.writableDatabase
        var create =
                "CREATE TABLE IF NOT EXISTS " +
                        "SavedList " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "category VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreLOI " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "average_volume VARCHAR(256)," +
                        "warrant_ticker VARCHAR(256)," +
                        "target_focus VARCHAR(256)," +
                        "underwriters VARCHAR(256)," +
                        "IPO_date VARCHAR(256)," +
                        "deadline_date VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "DefAgreement " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "volume_average VARCHAR(256)," +
                        "target VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "OptionChads " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "market_cap VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "current_volume VARCHAR(256)," +
                        "average_volume VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreUnitSplit " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "unit_warrant_details VARCHAR(256)," +
                        "estimated_trust_size VARCHAR(256)," +
                        "prominent_leadership VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"

        db?.execSQL(create)

        create =
                "CREATE TABLE IF NOT EXISTS " +
                        "PreIPO " +
                        "(" +
                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "ticker VARCHAR(256)," +
                        "name VARCHAR(256)," +
                        "estimated_trust_value VARCHAR(256)," +
                        "management_team VARCHAR(256)," +
                        "target_focus VARCHAR(256)" +
                        ")"

        db?.execSQL(create)
    }

    fun closeDB(){
        val db = this.writableDatabase
        db.close()
    }


}