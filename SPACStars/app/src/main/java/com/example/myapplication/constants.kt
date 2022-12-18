

package com.example.myapplication

import android.widget.TableRow
import android.widget.TextView

object constants {
    val worksheetsStartingRow: Map<String, String> = mapOf(
        "Pre+LOI" to "A5",
        "Definitive+Agreement" to "A4",
        "Option+Chads" to "A3",
        "Pre+Unit+Split" to "A3",
        "Pre+IPO" to "A3",
        "Warrants+(Testing)" to "A2"
    )
    val categoryInfoColumn: Map<String, Int> = mapOf(
        "Pre+LOI" to 15,
        "Definitive+Agreement" to 14,
        "Option+Chads" to 15,
        "Pre+Unit+Split" to 6,
        "Pre+IPO" to 2,
        "Warrants+(Testing)" to 12
    )
    val categoryInfoLabel: Map<String, String> = mapOf(
        "Pre+LOI" to "Current Volume",
        "Definitive+Agreement" to "Current Volume",
        "Option+Chads" to "Current Volume",
        "Pre+Unit+Split" to "Est. Trust Size",
        "Pre+IPO" to "Est. Trust Value",
        "Warrants+(Testing)" to "Current Volume"
    )

    val categoryInfoDB: Map<String, Int> = mapOf(
            "Pre+LOI" to 4,
            "Definitive+Agreement" to 3,
            "Option+Chads" to 4,
            "Pre+Unit+Split" to 3,
            "Pre+IPO" to 2
    )
    val sheetID: String = "1dZOPswJcmPQ5OqTw7LNeTZOXklnmD-n7fyohbkRSsFE"
    val apikey: String = "AIzaSyCZP2fBW638Gip01kDHMbHLaM84hWwU7uo"

    val SPACColumns: Map<String, Int> = mapOf(
            "Pre+LOI" to 11,
            "Definitive+Agreement" to 6,
            "Option+Chads" to 6,
            "Pre+Unit+Split" to 6,
            "Pre+IPO" to 5,
            "Warrants+(Testing)" to 12
    )

    val SPACColumnName: Map<String, Map<String,Int>> = mapOf(
            "Pre+LOI" to mapOf(
                        "ticker" to 0,
                        "name" to 1,
                        "market_cap" to 2,
                        "estimated_trust_value" to 3,
                        "current_volume" to 15,
                        "average_volume" to 16,
                        "warrant_ticker" to 18,
                        "target_focus" to 26,
                        "underwriters" to 27,
                        "IPO_date" to 28,
                        "deadline_date" to 30
                        ),
            "Definitive+Agreement" to mapOf(
                    "ticker" to 0,
                    "name" to 1,
                    "market_cap" to 2,
                    "current_volume" to 14,
                    "volume_average" to 15,
                    "target" to 17
            ),
            "Option+Chads" to mapOf(
                    "ticker" to 0,
                    "name" to 1,
                    "market_cap" to 2,
                    "estimated_trust_value" to 3,
                    "current_volume" to 15,
                    "average_volume" to 16
            ),
            "Pre+Unit+Split" to mapOf(
                    "ticker" to 0,
                    "name" to 1,
                    "unit_warrant_details" to 5,
                    "estimated_trust_size" to 6,
                    "prominent_leadership" to 8,
                    "target_focus" to 9
            ),
            "Pre+IPO" to mapOf(
                    "ticker" to 0,
                    "name" to 1,
                    "estimated_trust_value" to 2,
                    "management_team" to 3,
                    "target_focus" to 4
            )
    )


    val SPACTableName: Map<String, String> = mapOf(
            "Pre+LOI" to "PreLOI",
            "Definitive+Agreement" to "DefAgreement",
            "Option+Chads" to "OptionChads",
            "Pre+Unit+Split" to "PreUnitSplit",
            "Pre+IPO" to "PreIPO",
            "Warrants+(Testing)" to "Current Volume"
    )



    //parameters:
    // list = of arrays, each array is in the format of [ticker, name, information] (all Strings)
    // index = which index of the individual array to sort by (example: index 0 would sort by ticker
    // type = String or Int, determines if you want to sort the information by alphabetical or numeric order (usually only the information is sorted numerically)
    // isDescending = if you want the list sorted in descending order

    //returns a list of arrays, each array is in the format of [ticker, name, information] (all Strings)
    fun sortingOrder(list: MutableList<Array<String>>, index: Int, type: String, isDescending: Boolean): MutableList<Array<String>>{
        var sorted =
            if(type == "Int"){
                list.sortedBy { it[index].replace("$", "")
                        .replace(",","")
                        .replace(" ","")
                        .toInt() }.toMutableList()

            }else{
                list.sortedBy { it[index].toLowerCase() }.toMutableList()
            }
        if(isDescending){
            sorted = sorted.asReversed()
        }
        return sorted
    }

    fun sortTableRows(rows: MutableList<TableRow>, index: Int, isDescending: Boolean): MutableList<TableRow>{
        var sorted = if(index == 0){
            rows.sortedBy { (it.findViewById(R.id.ticker) as TextView).text.toString().toLowerCase() }.toMutableList()
        }else{
            rows.sortedBy { (it.findViewById(R.id.name) as TextView).text.toString().toLowerCase() }.toMutableList()
        }
        if(isDescending){
            sorted = sorted.asReversed()
        }
        return sorted
    }
}