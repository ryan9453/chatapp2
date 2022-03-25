package com.ryan.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class RoomViewModel : ViewModel() {
    // 將直播間資料設定成 LiveData以便觀察
    val chatRooms = MutableLiveData<List<Lightyear>>()
    val searchRooms = MutableLiveData<List<Lightyear>>()



    // 當呼叫此方法時就取得直播間資料，並改變該 LiveData的值
    fun getAllRooms() {
        // viewModel裡專用的協程方法
        // 因為這次的耗時工作是「存取」網路資料，所以情境物件設定「IO」
        viewModelScope.launch(Dispatchers.IO) {

            // 先將回應的json轉成字串
            val json = URL("https://api.jsonserve.com/qHsaqy").readText()
            // 在這前提要先解析過一次他回應的 json結構
            // 並新增一個接收他 json結構的類別
            // 將 json字串建立成一個類別物件
            val response = Gson().fromJson(json, ChatRooms::class.java)
            chatRooms.postValue(response.result.lightyear_list)
        }
    }
    fun getSearchRooms(keywords : String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 先將回應的json轉成字串
            val json = URL("https://api.jsonserve.com/qHsaqy").readText()
            // 在這前提要先解析過一次他回應的 json結構
            // 並新增一個接收他 json結構的類別
            // 將 json字串建立成一個類別物件
            val response = Gson().fromJson(json, ChatRooms::class.java)
            val searchKeyMap = mutableMapOf<String, Lightyear>()
            var keysList = mutableListOf<String>()
            val resultRoomsSet = mutableSetOf<Lightyear>()

            response.result.lightyear_list.forEach {
                searchKeyMap.put(it.nickname, it)
                searchKeyMap.put(it.stream_title, it)
                searchKeyMap.put(it.tags, it)
                keysList.add(it.nickname)
                keysList.add(it.stream_title)
                keysList.add(it.tags)
            }
            if (keywords == "") {
                resultRoomsSet.clear()
            } else {
                resultRoomsSet.clear()
                keysList.forEach { key ->
                    if (keywords in key) {
                        searchKeyMap[key]?.let {
                                matchRoom -> resultRoomsSet.add(matchRoom)
                        }
                    }
                }
            }
            searchRooms.postValue(resultRoomsSet.toList())
        }
    }

    fun getHitRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = URL("https://api.jsonserve.com/qHsaqy").readText()
            val response = Gson().fromJson(json, ChatRooms::class.java)
            val hitKeyMap = mutableMapOf<Int, Lightyear>()
            var keysList = mutableListOf<Int>()
            val resultRooms = mutableListOf<Lightyear>()

            response.result.lightyear_list.forEach {
                hitKeyMap.put(it.online_num, it)
                keysList.add(it.online_num)
            }

            keysList.sortDescending()

            keysList.forEach {
                hitKeyMap[it]?.let { sortedRoom -> resultRooms.add(sortedRoom) }
            }
            chatRooms.postValue(resultRooms)
            Log.d("viewModel", "第一間房間是 = ${resultRooms[0].nickname}")
        }
    }

}