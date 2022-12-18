package com.tinyappco.spacliveprices.data

import com.tinyappco.spacliveprices.model.SPACLivePrices
import org.json.JSONObject
import java.net.URL


class DataSource {

    fun loadSPACs(): List<SPACLivePrices> {
        val finalList: MutableList<SPACLivePrices> = mutableListOf()
        val jsonArray = URL("https://sheets.googleapis.com/v4/spreadsheets/1D61Q4V_LwTXVCOedHkg-IROuZKTiJ25wg_qL75XvWlc/values/Sheet1!A2:C329?key=AIzaSyCZP2fBW638Gip01kDHMbHLaM84hWwU7uo").readText()
        val info = JSONObject(jsonArray).getJSONArray("values")

        val len = info.length() - 1

        finalList.add(0, SPACLivePrices("TICKER", "LIVE PRICE", "COMPANY NAME"))

        for(i in 0..len) {
            finalList.add(i + 1, SPACLivePrices(info.getJSONArray(i).getString(0),
                    info.getJSONArray(i).getString(1),
                    info.getJSONArray(i).getString(2)))
        }

        return finalList
    }
}