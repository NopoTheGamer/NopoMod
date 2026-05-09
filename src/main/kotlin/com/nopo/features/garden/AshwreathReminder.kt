package com.nopo.features.garden

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.IslandChange
import com.nopo.events.TickEvent
import com.nopo.module.Module
import com.nopo.screens.GuiEditor
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.Position
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.componentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


object AshwreathReminder : Module("ashwreath", NopoMod.config.ashwreath), TickEvent, GuiRendering, IslandChange, CommandRegistration {

    private fun getConfig() = config as AshwreathConfig
    private var display: Component? = null

    override fun onTick(totalTicks: Int) {
        if (!getConfig().enabled) return
         if (shouldTell()) {
             display = componentBuilder {
                 append(Utils.themedGradient("Ashwreath Collection Time! "))
                 appendEmoji("money_mouth")
             }
         } else {
            display = null
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

    override fun render(context: GuiGraphics) {
        if (!config.enabled || !HypixelUtils.onSkyblock()) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    private fun doRender(context: GuiGraphics) {
        val display = display ?: return
        context.drawString(Minecraft.getInstance().font, display, 0, 0, -1)
    }

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (!getConfig().enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (newIsland != IslandType.GARDEN) return
        if (shouldTell()) {
            DelayedRuns.schedule(200) {
                Utils.sendMessageToPlayer("Ashwreath Time!!")
            }
        }
        getConfig().lastCollected = System.currentTimeMillis()
        display = null
        ConfigManager.save()
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                "ashwreath" {
                    "setPos" {
                        runs { x: Int?, y: Int? ->
                            if (x == null) {
                                NopoMod.screenToOpen = GuiEditor(getConfig().pos) {
                                    doRender(it)
                                }
                            } else if (y == null) {
                                Utils.sendMessageToPlayer("Missing y argument")
                            } else {
                                getConfig().pos.x = x
                                getConfig().pos.y = y
                                Utils.sendMessageToPlayer("Updated position")
                                ConfigManager.save()
                            }
                        }
                    }
                }
            }
        }
    }
}

class AshwreathConfig(default: Boolean) : ModuleConfig(default = default) {

    @Expose
    var lastCollected = 0L

    @Expose
    var pos = Position()
}