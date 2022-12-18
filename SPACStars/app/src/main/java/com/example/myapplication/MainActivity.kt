package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.storageHandlers.DBHandlerBase
import com.example.myapplication.storageHandlers.DBHandlerSavedList
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){

    private var saved = mutableListOf<Array<String>>()
    private var listAdapter = SavedListAdapter(this, saved, this)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        //Set the xml page to use as display page. R.layout is resources folder (res) / layout folder, as in app/res/layout/activity_main.xml
        setContentView(R.layout.activity_main)

        getSavedList()
    }


    //source: https://developer.android.com/guide/topics/ui/menus
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
                this.recreate()
            }


          //R.id.showAll -> showListSelection = "Show All"
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

        if(constants.worksheetsStartingRow.containsKey(showListSelection)){
            val intent = Intent(this, CategoryList::class.java)
            intent.putExtra("key", showListSelection)
            startActivity(intent)
        }


        return super.onOptionsItemSelected(item)
    }

    fun checkFirstRun(): Boolean{

        //https://developer.android.com/reference/kotlin/android/content/SharedPreferences
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val prefsEditor = prefs.edit()

        if(prefs.getBoolean("firstTime", true)){
            prefsEditor.putBoolean("firstTime", false)
            prefsEditor.apply()
            return true
        }
        return false
    }

    fun getList(SPACtype: String): JSONArray {
        val startingRow: String? = constants.worksheetsStartingRow[SPACtype]
//        val jsondata = JSONObject(URL("https://sheets.googleapis.com/v4/spreadsheets/$sheetID/values/$SPACtype!$startingRow:AF?key=$apikey").readText())

        val urlconn = URL("https://sheets.googleapis.com/v4/spreadsheets/${constants.sheetID}/values/$SPACtype!$startingRow:AF?key=${constants.apikey}")

        var jsonResult = ""

        try{
            jsonResult = urlconn.readText()
        }catch(e: Exception){
            println("no internet")
        }


        val jsondata = JSONObject(jsonResult)

        val SPAClist = jsondata.getJSONArray("values")
        return SPAClist
    }

    private fun getSavedList(){


        val db = DBHandlerSavedList(this)
        saved.addAll(db.getAllSavedSPAC())

        if(checkFirstRun() && saved.isEmpty()){
            println("first time")
            thread(start = true){

                val category = "Pre+IPO"

                val datalist = getList(category)

                val len = datalist.length()
                val dataMap: Map<String, Int> = constants.SPACColumnName[category] as Map<String, Int>
                val columnArray: Array<Map.Entry<String, Int>> = dataMap.entries.toTypedArray()
                columnArray.sortBy { it.value }
                val dbData: MutableList<Map<String, String>> = mutableListOf()
                val saveDefault: MutableList<Array<String>> = mutableListOf()
                for (i in 0 until len) {
                    if(datalist.getJSONArray(i).getString(0) != "N/A" &&
                            datalist.getJSONArray(i).getString(0) != "" &&
                            datalist.getJSONArray(i).getString(constants.categoryInfoColumn[category] as Int) != "#N/A"
                    ){
                        val sqlrowData: MutableMap<String, String> = mutableMapOf()
                        for ((k, v) in columnArray) {
                            sqlrowData[k] = datalist.getJSONArray(i).getString(v)
                        }
                        dbData.add(sqlrowData)

                        //adds three SPACs from the pre ipo list to the default saved list
                        if(i < 3){
                            saveDefault.add(arrayOf(datalist.getJSONArray(i).getString(0),
                                    datalist.getJSONArray(i).getString(1), "Pre+IPO"))
                        }
                    }

                }

                val catdb = DBHandlerBase(this)
                catdb.rebuildTable(category)
                catdb.bulkInsertSPAC(constants.SPACTableName[category], constants.SPACColumns[category], dbData)
                catdb.closeDB()

                for(i in saveDefault){
                    db.insertNewSavedSPAC(i[0], i[1], i[2])
                }
                saved.addAll(db.getAllSavedSPAC())
                db.closeDB()

                runOnUiThread{
                    createSavedList()
                    val firstText = findViewById<TextView>(R.id.firstlabel)
                    firstText.text = "Find SPACs from Listings to save!"
                }
            }
        }else{
            println("not first time")
            createSavedList()
            db.closeDB()
        }

        //https://suragch.medium.com/updating-data-in-an-android-recyclerview-842e56adbfd8


    }

    fun createSavedList(){
        listAdapter.notifyDataSetChanged()

        val viewList: RecyclerView = findViewById(R.id.recyclerViewSaved)
        viewList.adapter = listAdapter
        viewList.layoutManager = LinearLayoutManager(this)
        viewList.setHasFixedSize(true)

        //Set up search interface
        val searchtext = findViewById<TextView>(R.id.mainsearchtext)
        val search = findViewById<Button>(R.id.mainsearchbutton)
        search.setOnClickListener {search(searchtext, viewList)}
        //If there is no saved lists, tell user to add some
        if(saved.isEmpty()){
            searchtext.hint = "Save SPACs from the listings!"
            searchtext.isEnabled = false
            search.visibility = View.GONE
        }
    }

    fun removeButtonHandler(view: View) {
        val parent = view.parent.parent as View
        val ticker = parent.findViewById<TextView>(R.id.ticker).text
//        val name = parent.findViewById<TextView>(R.id.name)
        println(saved.indexOfFirst { it[0] == ticker })
        val db = DBHandlerSavedList(this)
        db.removeSPAC(ticker.toString())
        saved.removeAt(saved.indexOfFirst { it[0] == ticker })
        println(saved.size)
        listAdapter.notifyDataSetChanged()
    }

    fun refreshButtonHandler(view: View){
        this.recreate()
    }

    //Search the saved list
    fun search(text: TextView, viewlist: RecyclerView){
        val query = text.text.toString().toUpperCase()
        val searchresults: MutableList<Array<String>> = mutableListOf()
        if(query.isEmpty()){
            listAdapter = SavedListAdapter(this, saved, this)
            viewlist.adapter = listAdapter
            listAdapter.notifyDataSetChanged()
        }
        else {
            for (i in saved) {
                for (j in i) {
                    if (j.toUpperCase().contains(query)) {
                        searchresults.add(0, i)
                        break
                    }
                }
            }
            listAdapter = SavedListAdapter(this, searchresults, this)
            viewlist.adapter = listAdapter
            listAdapter.notifyDataSetChanged()
        }
    }

}