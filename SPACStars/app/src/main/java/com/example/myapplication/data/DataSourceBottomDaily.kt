package com.example.myapplication.data

import com.example.myapplication.model.SPACBottomDailyPriceChange
import org.json.JSONObject
import java.net.URL


class DataSourceBottomDaily {

    fun loadSPACs(): List<SPACBottomDailyPriceChange> {
        val list: MutableList<SPACBottomDailyPriceChange> = mutableListOf()
        val jsonArray = URL("https://sheets.googleapis.com/v4/spreadsheets/1D61Q4V_LwTXVCOedHkg-IROuZKTiJ25wg_qL75XvWlc/values/Daily % Change!A2:E265?key=AIzaSyCZP2fBW638Gip01kDHMbHLaM84hWwU7uo").readText()
        val info = JSONObject(jsonArray).getJSONArray("values")

        val len = info.length() - 1

//        finalList.add(0, SPACLivePrices("TICKER", "LIVE PRICE", "COMPANY NAME"))

        for(i in 0..len) {
            var company_name = info.getJSONArray(i).getString(4)
            if(company_name.length > 15){
                company_name = company_name.slice(IntRange(0,14)) + "..."
            }
            list.add(i, SPACBottomDailyPriceChange(info.getJSONArray(i).getString(0),
                info.getJSONArray(i).getString(1),
                info.getJSONArray(i).getString(3).toFloatOrNull(),
                company_name))
        }

        val finalList: MutableList<SPACBottomDailyPriceChange> = mutableListOf()

        for(j in 0..len) {
            if (list[j].stringResourceId3 != null){
                list[j].stringResourceId3 = "%.${2}f".format(list[j].stringResourceId3?.times(100.0)?.toFloat()).toFloatOrNull()
                finalList.add(list[j])
            }
        }

        finalList.sortBy { SPACBottomDailyPriceChange -> SPACBottomDailyPriceChange.stringResourceId3 }

        return finalList.take(10)
    }
}