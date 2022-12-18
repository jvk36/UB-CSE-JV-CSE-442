package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.ItemAdapter
import com.example.myapplication.data.DataSource
import com.example.myapplication.model.SPACLivePrices
import kotlin.concurrent.thread

class SPACLivePricesMain : AppCompatActivity() {

    var myDataset: List<SPACLivePrices> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spac_live_main)

        updateUI()
    }

    fun updateUI() {
        val searchtext = findViewById<TextView>(R.id.livesearch)
        val search = findViewById<Button>(R.id.livesearchbutton)
        thread (start=true) {
            // Initialize data.
            myDataset = DataSource().loadSPACs()

            this@SPACLivePricesMain.runOnUiThread(Runnable {
                val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                recyclerView.adapter = ItemAdapter(this, myDataset)

                // Use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                recyclerView.setHasFixedSize(true)
            })
            search.setOnClickListener { searchspacs(searchtext) }
        }
    }

    //Spac Search Function
    fun searchspacs(text: TextView){
        val searchresults: MutableList<SPACLivePrices> = mutableListOf()
        val query = text.text.toString().toUpperCase()
        if(query.isEmpty()){
            val listAdapter = ItemAdapter(this, myDataset)
            val viewList: RecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            viewList.adapter = listAdapter
        }
        else {
            for (i in myDataset) {
                    if (i.FullName.toUpperCase().contains(query) || i.stringResourceId1.toUpperCase().contains(query)) {
                        searchresults.add(i)
                }
            }
            val listAdapter = ItemAdapter(this, searchresults)
            val viewList: RecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            viewList.adapter = listAdapter
        }
    }

    fun refreshButtonHandler(view: View){
        this.recreate()
    }

}