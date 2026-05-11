package com.nopo.features.meta

import com.nopo.NopoMod
import com.nopo.events.GuiRendering
import com.nopo.events.IslandChange
import com.nopo.events.ScoreboardChange
import com.nopo.module.FeatureModule
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.network.chat.Component

object DebugModule : FeatureModule("debug", NopoMod.config.debug, shouldBeHidden = { !Utils.isDevAllowed() }), IslandChange, ScoreboardChange, GuiRendering {

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (!config.enabled) return
        Utils.sendMessageToPlayer("new $newIsland old $oldIsland")
    }

    private val crapLines = Regex("(?:.*\\d+:\\d+[ap]m .)|(?:Carnival \\d+:\\d+:\\d+)")

    override fun onScoreboardChange(
        added: List<Component>,
        removed: List<Component>,
        new: List<Component>,
        old: List<Component>,
    ) {
        if (!config.enabled) return
        val added = added.filterNot { crapLines.matches(it.string.cleanColor()) }
        val removed = removed.filterNot { crapLines.matches(it.string.cleanColor()) }

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

    var lastX = 0
    var lastY = 0

    override fun render(context: GuiGraphics) {
        if (!config.enabled) return
        val screen = Minecraft.getInstance().screen
        if (screen != null && screen !is ChatScreen) {
            val x = Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().window).toInt()
            val y = Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().window).toInt()
            lastX = x
            lastY = y
        }
        context.drawString(Minecraft.getInstance().font, "x: $lastX y: $lastY", lastX, lastY, -1)
    }

}