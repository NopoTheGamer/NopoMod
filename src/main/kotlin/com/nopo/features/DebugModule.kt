package com.nopo.features

import com.nopo.module.Module
import com.nopo.NopoMod
import com.nopo.events.IslandChange
import com.nopo.events.ScoreboardChange
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import net.minecraft.network.chat.Component

object DebugModule : Module("debug", NopoMod.config.debug, dev = true), IslandChange, ScoreboardChange {

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (!config.enabled) return
        Utils.sendMessageToPlayer("new $newIsland old $oldIsland")
    }

    val timeRegex = Regex(".*\\d+:\\d+[ap]m .")

    override fun onScoreboardChange(
        added: List<Component>,
        removed: List<Component>,
        new: List<Component>,
        old: List<Component>,
    ) {
        if (!config.enabled) return
        val added = added.filterNot { timeRegex.matches(it.string.cleanColor()) }
        val removed = removed.filterNot { timeRegex.matches(it.string.cleanColor()) }

        if (added.isNotEmpty()) {
            Utils.sendMessageToPlayer("Added: ")
            for (component in added) {
                Utils.sendMessageToPlayer(component)
            }
        }
        if (removed.isNotEmpty()) {
            Utils.sendMessageToPlayer("Removed: ")
            for (component in removed) {
                Utils.sendMessageToPlayer(component)
            }
        }
    }

}