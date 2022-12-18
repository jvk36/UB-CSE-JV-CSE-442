package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.storageHandlers.DBHandlerSavedList
import org.json.JSONArray


class TickerListAdapter(private val context: Context,
                        private val listing: MutableList<Array<String>>,
                        private val infoLabel: String?,
                        private val category: String, private val infoMap: MutableMap<String,Array<String>>)
    : RecyclerView.Adapter<TickerListAdapter.ListViewHolder>(){

    //source: https://www.youtube.com/watch?v=afl_i6uvvU0

//    private val context: Activity, private val title: Array<String>, private val subtitle: Array<String>, private val extra: Array<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val item: View = LayoutInflater.from(parent.context).inflate(R.layout.list_display, parent, false)
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
        holder.infoView.text = current[2]
        holder.labelView.text = infoLabel

        holder.itemView.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)
            val spacdata = infoMap[current[0]] as Array<String>
            val db = DBHandlerSavedList(context)
            var alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            when(category){

                "Pre+LOI" -> {
                    //Set the message of the alert window that appears
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
                    alertstring = "Ticker: " + spacdata[0] + "\n\nCompany Name: " + spacdata[1]
                    /*  This sets an "OK" button in the dialog window that
                        doesn't currently do anything except close the window
                        and print a message to the console  */
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE LOI")
                    }
                    if(!db.getSavedSPACExists(current[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE LOI")
                            db.insertNewSavedSPAC(current[0], current[1], category)
                        }
                    }

                    //Set the title for the alert window to the SPAC name
                    alert.setTitle(spacdata[1])

                    //Display the window to the user
                    alert.create().show()
                }

                "Definitive+Agreement" -> {

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
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, DEFINITIVE AGREEMENT")
                    }
                    if(!db.getSavedSPACExists(current[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC DEFINITIVE AGREEMENT")
                            db.insertNewSavedSPAC(current[0], current[1], category)
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }

                "Option+Chads" -> {
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
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, OPTION CHADS")
                    }
                    if(!db.getSavedSPACExists(current[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC OPTION CHADS")
                            db.insertNewSavedSPAC(current[0], current[1], category)
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }

                "Pre+Unit+Split" -> {

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
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE UNIT SPLIT")
                    }
                    if(!db.getSavedSPACExists(current[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE UNIT SPLIT")
                            db.insertNewSavedSPAC(current[0], current[1], category)
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }

                "Pre+IPO" -> {
                    if(preference.getBoolean("preipo_etv", true)){
                        alertstring += "\n\nEstimated Trust Value: " + spacdata[2].toString()
                    }
                    if(preference.getBoolean("preipo_managementteam", true)){
                        alertstring += "\n\nManagement Team: " + spacdata[3].toString()
                    }
                    if(preference.getBoolean("preipo_targetfocus", true)){
                        alertstring += "\n\nTarget Focus: " + spacdata[4].toString()
                    }
                    alert.setMessage(alertstring)
                    alertstring = "Ticker: " + spacdata[0]+ "\n\nCompany Name: " + spacdata[1]
                    alert.setPositiveButton("OK"){ _, _ -> println("POSITIVE PRESSED, PRE IPO")
                    }
                    if(!db.getSavedSPACExists(current[0])){
                        alert.setNegativeButton("SAVE"){ _, _ ->
                            println("NEGATIVE PRESSED, SAVE SPAC PRE IPO")
                            db.insertNewSavedSPAC(current[0], current[1], category)
                        }
                    }
                    alert.setTitle(spacdata[1])
                    alert.create().show()
                }


            }
        }
    }

//    fun onClick(item: View){
//
//    }

    class ListViewHolder(item: View): RecyclerView.ViewHolder(item){
        val tickerView: TextView = item.findViewById(R.id.ticker)
        val nameView: TextView = item.findViewById(R.id.name)
        val infoView: TextView = item.findViewById(R.id.info)
        val labelView: TextView = item.findViewById(R.id.label)
    }

    //ArrayAdapter<String>(context, R.layout.list_display, title){



    //source: https://www.javatpoint.com/kotlin-android-custom-listview
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val inflater: LayoutInflater = LayoutInflater.from(context)
//        val item = inflater.inflate(R.layout.list_display, null, true)
//
//        val ticker = item.findViewById<View>(R.id.ticker) as TextView
//        val name = item.findViewById<View>(R.id.name) as TextView
//
//        ticker.text = title[position]
//        name.text = subtitle[position]
//
//        return item
//    }
}