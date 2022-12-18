package com.example.myapplication

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

import com.example.myapplication.constants.worksheetsStartingRow
import com.example.myapplication.constants.apikey
import com.example.myapplication.constants.sheetID
import com.example.myapplication.constants.sortTableRows
import com.example.myapplication.constants.SPACColumnName
import com.example.myapplication.constants.SPACColumns
import com.example.myapplication.constants.SPACTableName
import com.example.myapplication.constants.categoryInfoColumn
import com.example.myapplication.constants.categoryInfoLabel
import com.example.myapplication.storageHandlers.*
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.w3c.dom.Text
import java.net.URL
import kotlin.concurrent.thread

class ShowListing : AppCompatActivity() {

    private var tableRows: MutableList<TableRow> = mutableListOf()
    private var loadeddata = 0
    private var fullList: MutableList<Array<String>> = mutableListOf()
    private val screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_listing)

//        setContentView(R.layout.activity_category_list2)
        //Initialize values for updating the SPAC table
        val table = findViewById<TableLayout>(R.id.listingtable)
        val context = applicationContext
        val searchtext = findViewById<TextView>(R.id.searchinput)
        searchtext.hint = "Loading SPACs..."

        //Update the titlebar from "SPAC Stars" to "Show Listing"
        val titlebar: ActionBar? = supportActionBar
        if (titlebar != null) {
            titlebar.title = "Show Listings"
            titlebar.subtitle = "Show All"
        }

        val spinner: Spinner = findViewById(R.id.sortDropdown)
        val items: Array<String> =
                arrayOf("Select Sorting Order",
                        "Ticker (A-Z)",
                        "Ticker (Z-A)",
                        "Name (A-Z)",
                        "Name (Z-A)"
                )

        val parameterMap: Array<Triple<Int, String, Boolean>> = arrayOf(
                Triple(0, "String", false),
                Triple(0, "String", true),
                Triple(1, "String", false),
                Triple(1, "String", true)
        )

        val dropdownAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = dropdownAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                println(parent.getItemAtPosition(position).toString())
                if(position != 0){
                    val index = items.indexOfFirst { it == parent.getItemAtPosition(position).toString() } - 1
                    alterTable(parameterMap[index], table)
                }
//                viewList.layoutManager = LinearLayoutManager(this)
//                viewList.setHasFixedSize(true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }


        //Create the first row for the table that shows "TICKER  SPAC NAME   CATEGORY"
//        addFirstRow(context, table)

        //Get the data for each category, starts on different threads

        getdata("Pre+LOI", table)
        getdata("Definitive+Agreement", table)
//        getdata("Option+Chads", table)
        getdata("Pre+Unit+Split", table)
        getdata("Pre+IPO", table)

        val search = findViewById<Button>(R.id.searchbutton)
        //Once all the data is loaded, enable the search button
        load(search, table, searchtext)
    }

    //Wait until data is retrieved to allow search
    fun load(button: Button, table: TableLayout, searchtext: TextView){
        thread(start = true) {
            while(loadeddata < 4){"wait for data to load before search becomes available"}
            searchtext.hint = "Search..."
            button.setOnClickListener { searchTable(table, searchtext) }
            println("search implemented")
        }
    }

    fun getdata(category: String, table: TableLayout){
        val displaycategory = category.replace("+", " ")
        println("Getting Data: $displaycategory")

        var db = DBHandlerBase(applicationContext)
        val dbPull = db.getAllSPACData(db.writableDatabase, SPACTableName[category], SPACColumns[category])
        db.closeDB()



        println(dbPull)

        if(dbPull.isNotEmpty()){
            println(dbPull)
            fullList = dbPull
            addtablerows(table,displaycategory,dbPull)
            loadeddata += 1
        }
        else {

            thread(start = true) {
                val datalist = getList(category)

                val len = datalist.length()
                val dataMap: Map<String, Int> = SPACColumnName[category] as Map<String, Int>
                val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                columnArray.sortBy { it.value }
                val fullData: MutableList<Array<String>> = mutableListOf()
                val dbData: MutableList<Map<String, String>> = mutableListOf()
                for (i in 0 until len) {
                    if(datalist.getJSONArray(i).getString(0) != "N/A" &&
                        datalist.getJSONArray(i).getString(0) != "" &&
                        datalist.getJSONArray(i).getString(categoryInfoColumn[category] as Int) != "#N/A"
                    ){
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


                runOnUiThread { addtablerows(table, displaycategory, fullData) }
                loadeddata += 1
                println("Data Acquired: $displaycategory")


                db = DBHandlerBase(applicationContext)
                db.bulkInsertSPAC(SPACTableName[category], SPACColumns[category], dbData)
                db.closeDB()
                //cache into db


            }
        }
    }

    //Function for getting data from URl
    fun getList(SPACtype: String): JSONArray{
        val startingRow: String? = worksheetsStartingRow[SPACtype]
//        val jsondata = JSONObject(URL("https://sheets.googleapis.com/v4/spreadsheets/$sheetID/values/$SPACtype!$startingRow:AF?key=$apikey").readText())

        val urlconn = URL("https://sheets.googleapis.com/v4/spreadsheets/$sheetID/values/$SPACtype!$startingRow:AF?key=$apikey")

        var jsonResult = ""

        try{
            jsonResult = urlconn.readText()
        }catch(e: Exception){
            println("no internet")
//            return mutableListOf()
        }


        val jsondata = JSONObject(jsonResult)

        val SPAClist = jsondata.getJSONArray("values")
        return SPAClist
    }

    //Function for adding data entries to the table
    fun addtablerows(table: TableLayout, category: String, data: MutableList<Array<String>>) {
        val context = applicationContext

//        println(data.size)

        for (i in data) {
            val spacdata = i
            val tablerow = TableRow(context)
            val inflater = layoutInflater
            val rowView = inflater.inflate(R.layout.show_all_items, tablerow)

            val Tickerrow = rowView.findViewById<TextView>(R.id.ticker)
            val Namerow = rowView.findViewById<TextView>(R.id.name)
            val Categoryrow = rowView.findViewById<TextView>(R.id.label)

//            val Tickerrow = TextView(context)
//            val Namerow = TextView(context)
//            val Categoryrow = TextView(context)
            val darkgraycolor = "#333333"
            //If there is no Ticker associated, don't add it
            if (spacdata[0] != "" && spacdata[0] != "N/A") {
                //Add ticker, name, and category all to table, set a color for that text
//                Tickerrow.text = spacdata[0] + "\t"
                Tickerrow.text = spacdata[0]
                //Tickerrow.setTextColor(Color.parseColor(darkgraycolor))
//                tablerow.addView(Tickerrow, 0)

                Namerow.width = screenWidth - 40
//                Namerow.text = spacdata[1] + "\n"
                Namerow.text = spacdata[1]
                //Namerow.setTextColor(Color.parseColor(darkgraycolor))
//                tablerow.addView(Namerow, 1)

//                Categoryrow.text = "\t" + category
                Categoryrow.text = category
                //Sets the tag to be used when searching later, adds \t do separate ticker and name

//                tablerow.addView(rowView, 0)
                tablerow.tag = spacdata[0] + "\t" + spacdata[1]

            }

            if(tablerow.getChildAt(0) != null){
                //Set the row to display data on click
                onclicksetter(tablerow, category, spacdata)
                //Add the row the table
                table.addView(tablerow)
                tableRows.add(tablerow)
            }
        }

    }

    fun addFirstRow(context: android.content.Context, table: TableLayout){
//        val context = applicationContext
        val firstrow = TableRow(context)
        val Tickerrow = TextView(context)
        val Namerow = TextView(context)
        val Categoryrow = TextView(context)
        val Blackcolor = "#000000"
        Tickerrow.setTypeface(null, Typeface.BOLD_ITALIC)
        Namerow.setTypeface(null, Typeface.BOLD_ITALIC)
        Categoryrow.setTypeface(null, Typeface.BOLD_ITALIC)
        Tickerrow.text = "TICKER\t"
        //Tickerrow.setTextColor(Color.parseColor(Blackcolor))
        //Namerow.setTextColor(Color.parseColor(Blackcolor))
        //Categoryrow.setTextColor(Color.parseColor(Blackcolor))
        Namerow.text = "SPAC NAME\t"
        Categoryrow.text = "CATEGORY\t"
        firstrow.addView(Tickerrow, 0)
        firstrow.addView(Namerow, 1)
        firstrow.addView(Categoryrow, 2)
        table.addView(firstrow)
    }

    //rebuild table after sorting
    fun alterTable(selection: Triple<Int, String, Boolean>, table: TableLayout){
        tableRows = sortTableRows(tableRows, selection.first, selection.third)
        table.removeAllViews()
//        addFirstRow(applicationContext, table)
        for(i in tableRows){
            table.addView(i)
        }

    }

    //Make the table entry show more data when clicked, depends on category name
    fun onclicksetter(tablerow: TableRow, category: String, spacdata: Array<String>){
        val db = DBHandlerSavedList(applicationContext)
        //Load the user preferences
        val preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
        when(category){

            "Pre LOI" -> {
                //Set a boolean for not duplicating the data
                tablerow.setOnClickListener {
                    //Create a builder for the alert window
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
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
                tablerow.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
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

            "Option Chads" -> {
                tablerow.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                    if(preference.getBoolean("optionchads_marketcap", true)){
                        alertstring += "\n\nMarket Cap: " + spacdata[2]
                    }
                    if(preference.getBoolean("optionchads_esttrustvalue", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[3]
                    }
                    if(preference.getBoolean("optionchads_currentvolume", true)){
                        alertstring += "\n\nCurrent Volume: " + spacdata[4]
                    }
                    if(preference.getBoolean("optionchads_volumeaverage", true)){
                        alertstring += "\n\nAverage Volume " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, OPTION CHADS")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC OPTION CHADS")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1].toString())
                    alert.create().show()
                }
            }

            "Pre Unit Split" -> {
                tablerow.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                    if(preference.getBoolean("preunit_unit", true)){
                        alertstring += "\n\nUnit & Warrant Details: " + spacdata[2]
                    }
                    if(preference.getBoolean("preunit_ets", true)){
                        alertstring += "\n\nEstimated Trust Size: " + spacdata[3]
                    }
                    if(preference.getBoolean("preunit_pl", true)){
                        alertstring += "\n\nProminent Leadership / Directors / Advisors: " + spacdata[4]
                    }
                    if(preference.getBoolean("preunit_tf", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[5]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE UNIT SPLIT")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE UNIT SPLIT")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }

            "Pre IPO" -> {
                tablerow.setOnClickListener {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                    if(preference.getBoolean("preipo_etv", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[2]
                    }
                    if(preference.getBoolean("preipo_managementteam", true)){
                        alertstring += "\n\nManagement Team: " + spacdata[3]
                    }
                    if(preference.getBoolean("preipo_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[4]
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){
                        _, _ -> println("POSITIVE PRESSED, PRE IPO")
                    }
                    if(!db.getSavedSPACExists(spacdata[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE IPO")
                            db.insertNewSavedSPAC(spacdata[0], spacdata[1], category.replace(" ", "+"))
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }
            }
        }
        db.closeDB()
    }

    //rebuild table after searching
    fun searchTable(table: TableLayout, text: TextView){
        println("searching...")
        val query = text.text.toString().toUpperCase()
        table.removeAllViews()
//        addFirstRow(applicationContext, table)
        //Reset when search is empty, otherwise only display items that match the search
        if(query.isEmpty()){
            for(i in tableRows){
                    table.addView(i)
            }
        }
        else{
        for(i in tableRows){
            if(i.tag.toString().toUpperCase().contains(query)) {
                table.addView(i)
            }
        }}

    }

    fun refreshButtonHandler(view: View){
        val table = findViewById<TableLayout>(R.id.listingtable)
        val searchtext = findViewById<TextView>(R.id.searchinput)
        val search = findViewById<Button>(R.id.searchbutton)
        resetsearch(searchtext, search)
        tableRows = mutableListOf()
        table.removeAllViews()
        val db = DBHandlerBase(this)
        db.rebuildTable("Pre+LOI")
        db.rebuildTable("Definitive+Agreement")
        db.rebuildTable("Pre+Unit+Split")
        db.rebuildTable("Pre+IPO")
        db.closeDB()
//        addFirstRow(applicationContext, table)
        getdata("Pre+LOI", table)
        getdata("Definitive+Agreement", table)
        getdata("Pre+Unit+Split", table)
        getdata("Pre+IPO", table)
        load(search, table, searchtext)
    }

    //Temporarily disable the search button while refreshing SPACs
    fun resetsearch(searchtext: TextView, button: Button){
        loadeddata = 0
        searchtext.hint = "Loading SPACs..."
        button.setOnClickListener {  }
    }

}