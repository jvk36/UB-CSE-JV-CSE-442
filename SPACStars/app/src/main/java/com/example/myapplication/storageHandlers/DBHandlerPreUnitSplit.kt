package com.example.myapplication.storageHandlers

import android.content.Context

class DBHandlerPreUnitSplit (context: Context)  : DBHandlerBase(context) {
//    override fun rebuildTable() {
//        val db = this.writableDatabase
//        db.execSQL("DROP TABLE IF EXISTS PreUnitSplit")
//        createTable()
//    }
//
//    override fun createTable() {
//        val db = this.writableDatabase
//        val create =
//                "CREATE TABLE IF NOT EXISTS " +
//                        "PreUnitSplit " +
//                        "(" +
//                        "sl_id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                        "ticker VARCHAR(256)," +
//                        "name VARCHAR(256)," +
//                        "unit_warrant_details VARCHAR(256)," +
//                        "estimated_trust_size VARCHAR(256)," +
//                        "prominent_leadership VARCHAR(256)," +
//                        "target_focus VARCHAR(256)" +
//                        ")"
//
//        db.execSQL(create)
//        db.close()
//    }

}