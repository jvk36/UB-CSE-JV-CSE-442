package com.agk.fetchrewardscodeexercise.data

import com.agk.fetchrewardscodeexercise.model.FetchRewardsData
import org.json.JSONArray
import java.net.URL

class DataSource {

    fun loadData(): List<FetchRewardsData> {
        val jsonArray = URL("https://fetch-hiring.s3.amazonaws.com/hiring.json").readText()
        val info = JSONArray(jsonArray)
        val len = info.length() - 1
        var finalList: MutableList<FetchRewardsData> = mutableListOf()

        for(i in 0..len) {
            val isValidName = info.getJSONObject(i).optString("name").toString()
            if (isValidName != "" && isValidName != "null"){
                finalList.add(0, FetchRewardsData(info.getJSONObject(i).optString("listId"),
                        info.getJSONObject(i).optString("name").toString(),
                        info.getJSONObject(i).optString("id")))
            }
        }

        finalList = finalList.sortedWith(compareBy<FetchRewardsData>{ it.resourceId1.toInt() }.thenBy { it.resourceId2 }).toMutableList()

        finalList.add(0, FetchRewardsData("ListId", "Name", "Id"))

        return finalList
    }
}