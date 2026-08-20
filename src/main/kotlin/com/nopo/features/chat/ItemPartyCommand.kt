package com.nopo.features.chat

import com.nopo.NopoMod
import com.nopo.events.ChatEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.SkyOceanUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.addSeparators
import com.nopo.utils.Utils.cleanColor
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component

object ItemPartyCommand : FeatureModule(
    "itemPartyCommand", NopoMod.config.itemPartyCommandConfig,
    ConfigData(
        Component.literal("!item Party Command"),
        Component.literal("Lets you do !item in Party Chat to see how many of an item you have")
    ),
    { !SkyOceanUtils.isSkyOceanLoaded }), ChatEvent {

    override fun onChat(message: Component, actionBar: Boolean) {
        if (!config.enabled) return
        sendItemMessage(message, "Party >", "pc")
    }

    fun sendItemMessage(message: Component, prefix: String, command: String) {
        val item = Utils.getPartyCommand(message, "!item", prefix) ?: return
        NopoMod.coroutineScope.launch {
            val data = SkyOceanUtils.getItemCount(item)
            if (data != null) {
                val itemCount = data.second.addSeparators()
                val itemName = data.first.string.cleanColor()
                Utils.sendCommandToServer("$command I have ${itemCount}x $itemName")
            }
        }
    }
}