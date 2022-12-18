package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.constants.apikey
import com.example.myapplication.constants.categoryInfoColumn
import com.example.myapplication.constants.categoryInfoLabel
import com.example.myapplication.constants.sheetID
import com.example.myapplication.constants.sortingOrder
import com.example.myapplication.constants.worksheetsStartingRow
import com.example.myapplication.constants.SPACColumns
import com.example.myapplication.constants.SPACColumnName
import com.example.myapplication.constants.SPACTableName
import com.example.myapplication.constants.categoryInfoDB
import com.example.myapplication.storageHandlers.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.net.URL
import kotlin.concurrent.thread



class CategoryList : AppCompatActivity() {

    private var results: MutableList<Array<String>> = mutableListOf()
    private var tickerMap: MutableMap<String, Array<String>> = mutableMapOf() //information for each ticker
    private var SPACtype: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list2)

        val searchtext = findViewById<TextView>(R.id.categorysearchinput)
        val search = findViewById<Button>(R.id.categorysearch)
        searchtext.hint = "Loading SPACs..."

        val extras = intent.extras
        val context = this

        if (extras != null) {
            SPACtype = extras.getString("key").toString()
        }

        val spinner:Spinner = findViewById(R.id.sortDropdown)
        val items: Array<String> =
                arrayOf("Select Sorting Order",
                        "Ticker (A-Z)",
                        "Ticker (Z-A)",
                        "Name (A-Z)",
                        "Name (Z-A)",
                        categoryInfoLabel[SPACtype] + "(ascending)",
                        categoryInfoLabel[SPACtype] + "(descending)"
                )

        val parameterMap: Array<Triple<Int, String, Boolean>> = arrayOf(
                Triple(0, "String", false),
                Triple(0, "String", true),
                Triple(1, "String", false),
                Triple(1, "String", true),
                Triple(2, "Int", false),
                Triple(2, "Int", true)
        )


        //sorting dropdown
        val dropdownAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = dropdownAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                println(parent.getItemAtPosition(position).toString())
                if(position != 0){
                    val index = items.indexOfFirst { it == parent.getItemAtPosition(position).toString() } - 1
                    val newOrder = sortingOrder(results, parameterMap[index].first, parameterMap[index].second, parameterMap[index].third)
                    results = newOrder
                    println(results)
                    val listAdapter = TickerListAdapter(context, results, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
                    val viewList: RecyclerView = findViewById(R.id.recyclerView)
                    viewList.adapter = listAdapter
                }
//                viewList.layoutManager = LinearLayoutManager(this)
//                viewList.setHasFixedSize(true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        getData(context, searchtext, search)
        searchtext.hint = "Search..."
        search.setOnClickListener { searchspacs(searchtext, SPACtype) }

    }

    private fun getData(context: Context, searchtext: TextView, search: Button){
        //get data from different types of categories through the db
        var dbPull:MutableList<Array<String>> = mutableListOf()


        val db = DBHandlerBase(this)
        dbPull = db.getAllSPACData(db.writableDatabase, SPACTableName[SPACtype], SPACColumns[SPACtype])
        db.closeDB()


        //check if db is empty, if not, use the db's pull, if it is, get from online API
        if(dbPull.isNotEmpty()){
            println(dbPull)
            val listDisplay: MutableList<Array<String>> = mutableListOf()
            for(i in dbPull){
                tickerMap[i[0]] = i
                listDisplay.add(arrayOf(i[0], i[1], i[categoryInfoDB[SPACtype] as Int]))
            }
            results = listDisplay
            val listAdapter = TickerListAdapter(context, results, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
            val viewList: RecyclerView = findViewById(R.id.recyclerView)
            viewList.adapter = listAdapter
            viewList.layoutManager = LinearLayoutManager(this)
            viewList.setHasFixedSize(true)
        }else {

            thread(start = true) {
                results = getList(SPACtype)
                this@CategoryList.runOnUiThread(Runnable {
                    val listAdapter = TickerListAdapter(context, results, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
                    val viewList: RecyclerView = findViewById(R.id.recyclerView)
                    viewList.adapter = listAdapter
                    viewList.layoutManager = LinearLayoutManager(this)
                    viewList.setHasFixedSize(true)
                })

                val dbData: MutableList<Map<String,String>> = mutableListOf()

                for(i in results){
                    val dataMap: Map<String, Int> = SPACColumnName[SPACtype] as Map<String, Int>
                    val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                    columnArray.sortBy { it.value }
                    val rowData: MutableMap<String, String> = mutableMapOf()
                    val info = tickerMap[i[0]] as Array<String>
                    for((k,v) in columnArray){
                        rowData[k] = info[columnArray.indexOfFirst { it.key == k }]
                    }

                    dbData.add(rowData as Map<String,String>)

                }

                val db = DBHandlerBase(this)
                db.bulkInsertSPAC(SPACTableName[SPACtype], SPACColumns[SPACtype], dbData)
                db.closeDB()


            }
        }
    }

    private fun getList(SPACtype: String): MutableList<Array<String>> {


        val startingRow: String? = worksheetsStartingRow[SPACtype]
        val extraIndex: Int = categoryInfoColumn[SPACtype] as Int
//        println("https://sheets.googleapis.com/v4/spreadsheets/$sheetID/values/$SPACtype!$startingRow:$extrasColumn?key=$apikey")
        val urlconn =
            URL("https://sheets.googleapis.com/v4/spreadsheets/$sheetID/values/$SPACtype!$startingRow:AF?key=$apikey")


        var jsonResult = ""

        try{
            jsonResult = urlconn.readText()
        }catch(e: Exception){
            println("no internet")
            return mutableListOf()
        }

        if(jsonResult == ""){
            return mutableListOf()
        }


        //if there is no internet, exception will occur

        val information: JSONObject = JSONObject(jsonResult)
        val rawSpacList = information.getJSONArray("values")
        val len = rawSpacList.length() - 1


        val finalList: MutableList<Array<String>> = mutableListOf()

        for(i in 0..len){
//            val extraIndex = rawSpacList.getJSONArray(i).length() - 1
            if(rawSpacList.getJSONArray(i).getString(0) != "N/A" &&
                rawSpacList.getJSONArray(i).getString(0) != "" &&
                    rawSpacList.getJSONArray(i).getString(extraIndex) != "#N/A"
            ){
                val innerArray: Array<String> = arrayOf(
                    rawSpacList.getJSONArray(i).getString(0),
                    rawSpacList.getJSONArray(i).getString(1).trim(),
                    rawSpacList.getJSONArray(i).getString(extraIndex)
                )
                finalList.add(innerArray)

                val dataMap: Map<String, Int> = SPACColumnName[SPACtype] as Map<String, Int>
                val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                columnArray.sortBy { it.value }
                val infoArr: MutableList<String> = mutableListOf()
                val rawRow = rawSpacList.getJSONArray(i)
                for((k,v) in columnArray){
                    infoArr.add(rawRow.getString(v))
                }

                tickerMap[rawSpacList.getJSONArray(i).getString(0)] = infoArr.toTypedArray()
            }
        }


        return finalList
    }

    fun refreshButtonHandler(view: View){
        thread(start = true) {
            results = getList(SPACtype)

//            println(results.joinToString())
            this@CategoryList.runOnUiThread(Runnable {
                val listAdapter = TickerListAdapter(this, results, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
                val viewList: RecyclerView = findViewById(R.id.recyclerView)
                viewList.adapter = listAdapter
                viewList.layoutManager = LinearLayoutManager(this)
                println("reset list")
                viewList.setHasFixedSize(true)
            })

            val dbData: MutableList<Map<String,String>> = mutableListOf()
            for(i in results){
                val dataMap: Map<String, Int> = SPACColumnName[SPACtype] as Map<String, Int>
                val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                columnArray.sortBy { it.value }
                val rowData: MutableMap<String, String> = mutableMapOf()
                val info = tickerMap[i[0]] as Array<String>
                for((k,v) in columnArray){
                    rowData[k] = info[columnArray.indexOfFirst { it.key == k }]
                }
                dbData.add(rowData as Map<String,String>)


            }
            val db = DBHandlerBase(this)
            db.rebuildTable(SPACtype)
            db.bulkInsertSPAC(SPACTableName[SPACtype], SPACColumns[SPACtype], dbData)
            db.closeDB()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menubar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var showListSelection: String = ""


        when (item.itemId) {

            R.id.searchsocialmedia -> {
                val intent = Intent(this, SearchSocialMedia::class.java)
                startActivity(intent)
            }

            R.id.all -> {
                val intent = Intent(this, SPACLivePricesMain::class.java)
                startActivity(intent)
            }

            R.id.top10DailyPriceChange -> {
                val intent = Intent(this, SPACTopDailyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.bottom10DailyPriceChange -> {
                val intent = Intent(this, SPACBottomDailyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.top10WeeklyPriceChange -> {
                val intent = Intent(this, SPACTopWeeklyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.bottom10WeeklyPriceChange -> {
                val intent = Intent(this, SPACBottomWeeklyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.top10MonthlyPriceChange -> {
                val intent = Intent(this, SPACTopMonthlyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.bottom10MonthlyPriceChange -> {
                val intent = Intent(this, SPACBottomMonthlyPriceChangeMain::class.java)
                startActivity(intent)
            }

            R.id.preferences -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.alertsetup -> {
                val intent = Intent(this, Alerts::class.java)
                startActivity(intent)
            }
            R.id.addremove -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.showAll -> {
                val intent = Intent(this, ShowListing::class.java)
                startActivity(intent)
            }
            R.id.preLOI -> showListSelection = "Pre+LOI"
            R.id.defAgree -> showListSelection = "Definitive+Agreement"
//            R.id.optionChads -> showListSelection = "Option+Chads"
            R.id.preUnit -> showListSelection = "Pre+Unit+Split"
            R.id.preIPO -> showListSelection = "Pre+IPO"
//            R.id.warrants -> showListSelection = "Warrants+(Testing)"
        }

        if(worksheetsStartingRow.containsKey(showListSelection)){
            val intent = Intent(this, CategoryList::class.java)
            intent.putExtra("key", showListSelection)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    //Spac Search Function
    fun searchspacs(text: TextView, SPACtype: String){
        val searchresults: MutableList<Array<String>> = mutableListOf()
        val query = text.text.toString().toUpperCase()
        if(query.isEmpty()){
            val listAdapter = TickerListAdapter(this, results, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
            val viewList: RecyclerView = findViewById(R.id.recyclerView)
            viewList.adapter = listAdapter
        }
        else {
            for (i in results) {
                for (j in i) {
                    if (j.toUpperCase().contains(query)) {
                        searchresults.add(i)
                        break
                    }
                }
            }
            val listAdapter = TickerListAdapter(this, searchresults, categoryInfoLabel[SPACtype], SPACtype, tickerMap)
            val viewList: RecyclerView = findViewById(R.id.recyclerView)
            viewList.adapter = listAdapter
        }
    }

}