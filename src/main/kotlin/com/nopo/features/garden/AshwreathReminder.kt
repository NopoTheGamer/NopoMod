package com.nopo.features.garden

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.PositionConfig
import com.nopo.events.GuiRendering
import com.nopo.events.IslandChange
import com.nopo.events.TickEvent
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.componentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


object AshwreathReminder : FeatureModule("ashwreath", NopoMod.config.ashwreath, shouldBeHidden = { !Utils.isDevAllowed() }), TickEvent, GuiRendering, IslandChange {

    private fun getConfig() = config as AshwreathConfig
    private val display: Component = componentBuilder {
        append(Utils.themedGradient("Ashwreath Collection Time! "))
        appendEmoji("money_mouth")
    }
    private var shouldShow = false

    override fun onTick(totalTicks: Int) {
        if (!getConfig().enabled) return
         if (shouldTell()) {
             shouldShow = true
         } else {
             shouldShow = false
        }
    }

    private fun shouldTell(): Boolean {
        val now = LocalDateTime.now()
        val last = Instant.ofEpochMilli(getConfig().lastCollected).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val sevenAM = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0))
        val tenPM = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0))
        if (now.isAfter(tenPM) && last.isBefore(tenPM)) {
            return true
        } else if (now.isAfter(sevenAM) && last.isBefore(sevenAM)) {
            return true
        }
        return false
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!config.enabled || !HypixelUtils.onSkyblock()) return
        if (!shouldShow) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphicsExtractor) {
        context.text(Minecraft.getInstance().font, display, 0, 0, -1)
    }

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (!getConfig().enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (newIsland != IslandType.GARDEN) return
        if (shouldTell()) {
            DelayedRuns.schedule(40) {
                Utils.sendMessageToPlayer("Ashwreath Time!!")
            }
        }
        getConfig().lastCollected = System.currentTimeMillis()
        shouldShow = false
        ConfigManager.save()
    }
}

class AshwreathConfig(default: Boolean) : PositionConfig(default = default) {
    @Expose
    var lastCollected = 0L
}