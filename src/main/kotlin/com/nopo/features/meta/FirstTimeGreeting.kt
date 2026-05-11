package com.nopo.features.meta

import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.IslandChange
import com.nopo.module.BaseModule
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object FirstTimeGreeting : BaseModule("first time join"), IslandChange {
    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (NopoMod.config.firstTime && newIsland == IslandType.HUB) {
            NopoMod.config.firstTime = false
            ConfigManager.save()
            Utils.sendMessageToPlayer(
                componentBuilder {
                    append("Hello !! This is your first time using the mod! Do ")
                    append("/nopo feature <module name> ") {
                        hover = Component.literal("Click to put in chat box")
                        suggest = "/nopo feature "
                        withColor(ChatFormatting.GOLD)
                    }
                    append("to toggle that feature.")
                    withColor(ChatFormatting.YELLOW)
                }
            )
        }
    }
}