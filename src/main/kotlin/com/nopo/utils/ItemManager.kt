package com.nopo.utils

import com.google.gson.stream.JsonReader
import com.nopo.config.ConfigManager
import com.nopo.data.SkyblockItem
import com.nopo.data.SkyblockItems
import java.io.StringReader
import java.net.URI
import java.net.URL

object ItemManager {

    private const val ITEMS_JSON = "https://api.hypixel.net/v2/resources/skyblock/items"
    private var items: SkyblockItems? = null

    fun downloadItemsJson() {
        try {
            val json = URL.of(URI.create(ITEMS_JSON), null).readText()
            items = ConfigManager.gson.fromJson<SkyblockItems>(JsonReader(StringReader(json)), SkyblockItems::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getById(id: String): SkyblockItem? {
        val items = items?.items ?: return null
        for (item in items) {
            if (item.id == id) return item
        }
        return null
    }

    fun getByName(name: String): SkyblockItem? {
        val items = items?.items ?: return null
        for (item in items) {
            if (item.name.equals(name, ignoreCase = true)) return item
        }
        return null
    }
}