package com.tinyappco.fetchrewardcodingexercise.data

import com.tinyappco.fetchrewardcodingexercise.model.FetchRewardsData
import org.json.JSONArray
import java.net.URL


class DataSource {

    fun loadData(): List<FetchRewardsData> {
        val list: MutableList<FetchRewardsData> = mutableListOf()
        val jsonArray = URL("https://fetch-hiring.s3.amazonaws.com/hiring.json").readText()
        val info = JSONArray(jsonArray)
        val len = info.length() - 1
        val finalList: MutableList<FetchRewardsData> = mutableListOf()

        finalList.add(0, FetchRewardsData("ListId", "Name", "Id"))

        for(i in 0..len) {
            list.add(i+1, FetchRewardsData(info.getJSONObject(i).optString("listId").toString(),
                    info.getJSONObject(i).optString("name").toString(),
                    info.getJSONObject(i).optString("id").toString()))
        }

        for(j in 0..len) {
            if (list[j].resourceId2 != "" && list[j].resourceId2 != "null"){
                finalList.add(list[j])
            }
        }

        val finalSortedList = finalList.sortedWith(compareBy<FetchRewardsData>{ it.resourceId1 }.thenBy { it.resourceId2 })

        return finalSortedList
    }
}
