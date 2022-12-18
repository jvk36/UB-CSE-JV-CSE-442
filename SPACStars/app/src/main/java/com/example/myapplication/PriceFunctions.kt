package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.example.myapplication.adapter.*
import com.example.myapplication.storageHandlers.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


object PriceFunctions {


    fun getList(SPACtype: String): JSONArray {
        val startingRow: String? = constants.worksheetsStartingRow[SPACtype]
        val jsondata = JSONObject(URL("https://sheets.googleapis.com/v4/spreadsheets/${constants.sheetID}/values/$SPACtype!$startingRow:AF?key=${constants.apikey}").readText())
        val SPAClist = jsondata.getJSONArray("values")
        return SPAClist
    }

    fun getdata(category: String, context: Context): MutableList<Array<String>>{
        var db = DBHandlerBase(context)
        var dbPull:MutableList<Array<String>> = mutableListOf()
        when(category){
            "Pre+LOI" -> {
                val db = DBHandlerPreLOI(context)
                dbPull = db.getAllSPACData(db.writableDatabase, constants.SPACTableName[category], constants.SPACColumns[category])
//                db.closeDB()
            }

            "Definitive+Agreement" -> {
                val db = DBHandlerDefAgreement(context)
                dbPull = db.getAllSPACData(db.writableDatabase, constants.SPACTableName[category], constants.SPACColumns[category])
//                db.closeDB()
            }

            "Option+Chads" -> {
                val db = DBHandlerOptionChads(context)
                dbPull = db.getAllSPACData(db.writableDatabase, constants.SPACTableName[category], constants.SPACColumns[category])
//                db.closeDB()
            }

            "Pre+Unit+Split" -> {
                val db = DBHandlerPreUnitSplit(context)
                dbPull = db.getAllSPACData(db.writableDatabase, constants.SPACTableName[category], constants.SPACColumns[category])
//                db.closeDB()
            }

            "Pre+IPO" -> {
                val db = DBHandlerPreIPO(context)
                dbPull = db.getAllSPACData(db.writableDatabase, constants.SPACTableName[category], constants.SPACColumns[category])
//                db.closeDB()
            }
        }
        db.closeDB()
        if(dbPull.isNotEmpty()){
            return dbPull
        }
        else {
            val fullData: MutableList<Array<String>> = mutableListOf()
            val t = Thread {
                val datalist = getList(category)
                val len = datalist.length()
                val dataMap: Map<String, Int> = constants.SPACColumnName[category] as Map<String, Int>
                val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                columnArray.sortBy { it.value }
                val dbData: MutableList<Map<String, String>> = mutableListOf()
                for (i in 0 until len) {
                    if (datalist.getJSONArray(i).getString(0) != "N/A" &&
                            datalist.getJSONArray(i).getString(0) != "" &&
                            datalist.getJSONArray(i).getString(constants.categoryInfoColumn[category] as Int) != "#N/A"
                    ) {
                        val rowData: MutableList<String> = mutableListOf()
                        val sqlrowData: MutableMap<String, String> = mutableMapOf()
                        for ((k, v) in columnArray) {
                            rowData.add(datalist.getJSONArray(i).getString(v))
                            sqlrowData[k] = datalist.getJSONArray(i).getString(v)
                        }
                        dbData.add(sqlrowData)
                        fullData.add(rowData.toTypedArray())
                    }
                }
                db = DBHandlerBase(context)
                db.bulkInsertSPAC(constants.SPACTableName[category], constants.SPACColumns[category], dbData)
                db.closeDB()
                //cache into db
            }
            t.start()
            t.join()
            return fullData
        }
    }

    //This function sometimes causes a ConcurrentModificationException
    //Seems like the list is still getting updated while the code is running
    fun getSPACdata(list: MutableList<Array<String>>, ticker: String): Array<String>{
        val iteration = list.iterator()
        while(iteration.hasNext()){
            val item = iteration.next()
            if(item[0] == ticker){
                return item
            }
        }
        return arrayOf("SPAC NOT FOUND")
    }

    fun onclicksetter(item: ItemAdapter.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[7] +
                            "\n\nCurrent Price: " + spacdata[2] +
                            "\n\nMarket Cap: " + spacdata[3] +
                            "\n\nEstimated Trust Value: " + spacdata[4] +
                            "\n\nCurrent Volume: " + spacdata[5] +
                            "\n\nAverage Volume: " + spacdata[6] +
                            "\n\nThis SPAC cannot be saved."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[7].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_bottomdaily(item: ItemAdapterBottomDailyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_topdaily(item: ItemAdapterTopDailyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_bottomweekly(item: ItemAdapterBottomWeeklyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_topweekly(item: ItemAdapterTopWeeklyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_bottommonthly(item: ItemAdapterBottomMonthlyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

    fun onclicksetter_topmonthly(item: ItemAdapterTopMonthlyPriceChange.ItemViewHolder, category: String, spacdata: Array<String>, context: Context){
        val db = DBHandlerSavedList(context)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                item.itemView.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    //Display data based on preferences chosen
                    if(preference.getBoolean("preloi_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("preloi_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("preloi_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("preloi_averagevolume", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    if(preference.getBoolean("preloi_warrantticker", true)){
                        alertstring += "\n\nWarrant Ticker: " + spacdata[6]
                    }
                    if(preference.getBoolean("preloi_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[7]
                    }
                    if(preference.getBoolean("preloi_underwriters", true)){
                        alertstring += "\n\nUnderwriters: " + spacdata[8]
                    }
                    if(preference.getBoolean("preloi_ipodate", true)){
                        alertstring += "\n\nIPO Date: " + spacdata[9]
                    }
                    if(preference.getBoolean("preloi_deadlinedate", true)){
                        alertstring += "\n\nDeadline Date: " + spacdata[10]
                    }
                    //Set the message of the alert window that appears
                    alert.setMessage(alertstring)
                    //Reset the string so that it doesn't display duplicate data when clicked again
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                    doesn't currently do anything except close the window
                    and print a message to the console  */
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }
            }

            "Definitive Agreement" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    if(preference.getBoolean("definitiveagreement_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("definitiveagreement_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[3]
                    }
                    if(preference.getBoolean("definitiveagreement_volumeaverage", true)){
                        alertstring += "\n\nVolume Average: " + spacdata[4]
                    }
                    if(preference.getBoolean("definitiveagreement_target", true)){
                        alertstring += "\n\nTarget: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "NOT_FOUND" -> {
                item.itemView.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(context)
                    alert.setMessage("Ticker: " + spacdata[0] +
                            "\n\nCompany Name: " + spacdata[2] +
                            "\n\nPrice Change Percentage: " + spacdata[1] +
                            "\n\nCurrent Price: " + spacdata[3] +
                            "\n\nNo further data can be found for this particular SPAC."
                    )
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, NOT_FOUND")
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

        }
        db.closeDB()
    }

}