package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

import com.example.myapplication.constants.SPACTableName
import com.example.myapplication.constants.SPACColumns
import com.example.myapplication.storageHandlers.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class SavedListAdapter(private val context: Context, private val listing: MutableList<Array<String>>, private val activity: MainActivity) : RecyclerView.Adapter<SavedListAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedListAdapter.ListViewHolder {
        val item: View = LayoutInflater.from(parent.context).inflate(R.layout.saved_list_items, parent, false)
        return ListViewHolder(item)
    }

    override fun getItemCount(): Int {
        return listing.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val current = listing[position]

        var name = current[1]

        if(name.length > 15){
            name = name.slice(IntRange(0,14)) + "..."
        }
        holder.tickerView.text = current[0]
        holder.nameView.text = name

        holder.itemView.setOnClickListener{

            //get data from sqlite
            val db = DBHandlerSavedList(context)
            val info = db.getSavedSPACInfo(current[0])
            val category = info[2]
//                db.closeDB()
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)

            when(category){

                "Pre+LOI" -> {

                    val spacdata = db.getSPACData(info[0], db.writableDatabase, SPACTableName[category], SPACColumns[category])
//                        infodb.closeDB()

                    //Set the message of the alert window that appears
                    alert.setMessage("Ticker: " + spacdata[0]
                            + "\n\nCompany Name: " + spacdata[1]
                            + "\n\nMarket Cap: " + spacdata[2]
                            + "\n\nEstimated Trust Value: " + spacdata[3]
                            + "\n\nCurrent Volume: " + spacdata[4]
                            + "\n\nAverage Volume " + spacdata[5]
                            + "\n\nWarrant Ticker: " + spacdata[6]
                            + "\n\nTarget Focus: " + spacdata[7]
                            + "\n\nUnderwriters: " + spacdata[8]
                            + "\n\nIPO Date: " + spacdata[9]
                            + "\n\nDeadline Date: " + spacdata[10]
                    )
                    /*  This sets an "OK" button in the dialog window that
                        doesn't currently do anything except close the window
                        and print a message to the console  */
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }

                "Definitive+Agreement" -> {

                    val spacdata = db.getSPACData(info[0], db.writableDatabase, SPACTableName[category], SPACColumns[category])


                    alert.setMessage("Ticker: " + spacdata[0]
                            + "\n\nCompany Name: " + spacdata[1]
                            + "\n\nMarket Cap: " + spacdata[2]
                            + "\n\nCurrent Volume: " + spacdata[3]
                            + "\n\nVolume Average: " + spacdata[4]
                            + "\n\nTarget: " + spacdata[5]
                    )
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }


                "Pre+Unit+Split" -> {

                    val spacdata = db.getSPACData(info[0], db.writableDatabase, SPACTableName[category], SPACColumns[category])


                    alert.setMessage("Ticker: " + spacdata[0]
                            + "\n\nCompany Name: " + spacdata[1]
                            + "\n\nUnit & Warrant Details: " + spacdata[2]
                            + "\n\nEstimated Trust Size: " + spacdata[3]
                            + "\n\nProminent Leadership / Directors / Advisors: " + spacdata[4]
                            + "\n\nTarget Focus: " + spacdata[5]
                    )
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE UNIT SPLIT")
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }

                "Pre+IPO" -> {

                    val spacdata = db.getSPACData(info[0], db.writableDatabase, SPACTableName[category], SPACColumns[category])

                    alert.setMessage("Ticker: " + spacdata[0]
                            + "\n\nCompany Name: " + spacdata[1]
                            + "\n\nEstimated Trust Value: " + spacdata[2]
                            + "\n\nManagement Team: " + spacdata[3]
                            + "\n\nTarget Focus: " + spacdata[4]
                    )
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE IPO")
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }


            }
            db.closeDB()
        }
    }

    class ListViewHolder(item: View): RecyclerView.ViewHolder(item){
        val tickerView: TextView = item.findViewById(R.id.ticker)
        val nameView: TextView = item.findViewById(R.id.name)
    }
}