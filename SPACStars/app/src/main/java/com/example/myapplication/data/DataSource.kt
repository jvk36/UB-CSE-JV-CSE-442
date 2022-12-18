package com.example.myapplication.data

import com.example.myapplication.model.SPACLivePrices
import org.json.JSONObject
import java.net.URL


class DataSource {

    fun loadSPACs(): List<SPACLivePrices> {
        val finalList: MutableList<SPACLivePrices> = mutableListOf()
        val jsonArray = URL("https://sheets.googleapis.com/v4/spreadsheets/1D61Q4V_LwTXVCOedHkg-IROuZKTiJ25wg_qL75XvWlc/values/SPAC Live Prices!A2:G265?key=AIzaSyCZP2fBW638Gip01kDHMbHLaM84hWwU7uo").readText()
        val info = JSONObject(jsonArray).getJSONArray("values")

        val len = info.length() - 1

//        finalList.add(0, SPACLivePrices("TICKER", "LIVE PRICE", "COMPANY NAME"))

        for(i in 0..len) {
            val currentSPAC = info.getJSONArray(i)
            var company_name = currentSPAC.getString(2)
            if(company_name.length > 15){
                company_name = company_name.slice(IntRange(0,14)) + "..."
            }
            finalList.add(
                    i,
                    SPACLivePrices(
                            currentSPAC.getString(0),
                            currentSPAC.getString(1),
                            company_name,
                            "Current Price",
                            currentSPAC.getString(3),
                            currentSPAC.getString(4),
                            currentSPAC.getString(5),
                            currentSPAC.getString(6),
                            currentSPAC.getString(2)
                    )
            )
        }

        return finalList
    }
}