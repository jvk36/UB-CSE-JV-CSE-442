package com.example.myapplication.storageHandlers

import android.content.Context

class DBHandlerPreLOI(context: Context) : DBHandlerBase(context) {

//    override fun rebuildTable() {
//        val db = this.writableDatabase
//        db.execSQL("DROP TABLE IF EXISTS PreLOI")
//        createTable()
//    }
//
//    override fun createTable() {
//        println("creating table")
//        val db = this.writableDatabase
//        val create =
//                "CREATE TABLE IF NOT EXISTS " +
//                        "PreLOI " +
//                        "(" +
//                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                        "ticker VARCHAR(256)," +
//                        "name VARCHAR(256)," +
//                        "market_cap VARCHAR(256)," +
//                        "estimated_trust_value VARCHAR(256)," +
//                        "current_volume VARCHAR(256)," +
//                        "average_volume VARCHAR(256)," +
//                        "warrant_ticker VARCHAR(256)," +
//                        "target_focus VARCHAR(256)," +
//                        "underwriters VARCHAR(256)," +
//                        "IPO_date VARCHAR(256)," +
//                        "deadline_date VARCHAR(256)" +
//                        ")"
//
//        db.execSQL(create)
//        db.close()
//    }

}