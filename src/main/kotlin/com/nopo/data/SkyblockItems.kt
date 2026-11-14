package com.nopo.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.nopo.utils.Rarity

data class SkyblockItems(
    @Expose
    val items: List<SkyblockItem>?
)

data class SkyblockItem(
    @Expose val name: String?,
    @Expose @SerializedName("tier") val rarity: Rarity?,
    @Expose @SerializedName("npc_sell_price") val npcSellPrice: Double?,
    @Expose val id: String?
)