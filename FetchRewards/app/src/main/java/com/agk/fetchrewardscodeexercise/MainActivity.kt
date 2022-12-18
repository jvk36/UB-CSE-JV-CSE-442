package com.agk.fetchrewardscodeexercise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.agk.fetchrewardscodeexercise.adapter.ItemAdapter
import com.agk.fetchrewardscodeexercise.data.DataSource
import com.agk.fetchrewardscodeexercise.model.FetchRewardsData
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*
        updateUI()
*/
        fetchData()
    }

    private fun fetchData() {
        val thread = Thread {
            val myDataset = DataSource().loadData()
            updateView(myDataset)
        }
        thread.start()
    }

    private fun updateView(data: List<FetchRewardsData>) {
        runOnUiThread{
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            recyclerView.adapter = ItemAdapter(this, data)

            // Use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true)
        }
    }

    fun updateUI() {
        thread (start=true) {
            // Initialize data.
            val myDataset = DataSource().loadData()

            this@MainActivity.runOnUiThread(Runnable {
                val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                recyclerView.adapter = ItemAdapter(this, myDataset)

                // Use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                recyclerView.setHasFixedSize(true)
            })
        }
    }
}