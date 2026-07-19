package com.nopo.features.combat

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.ListCommandExtras
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import kotlin.jvm.optionals.getOrNull

object KillCounter : FeatureModule("killCounter", NopoMod.config.killCounterConfig, ConfigData(
    Component.literal("Book Of Stats Kill Tracker"),
    Utils.componentBuilder {
        append("Tracks how many kills you have gotten since game launch with weapons that have a Book Of Stats")
    }
)), TickEvent, GuiRendering, CommandRegistration, ListCommandExtras {

    private val trackedKills: MutableMap<String, KillData> = mutableMapOf()
    private fun getConfig() = config as PositionConfig

    override fun onTick(totalTicks: Int) {
        val mainHandItem = Minecraft.getInstance().player?.mainHandItem ?: return
        val nbt = mainHandItem.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return
        val kills = nbt.getInt("stats_book").getOrNull() ?: return
        val uuid = nbt.getString("uuid").getOrNull() ?: return
        if (!trackedKills.contains(uuid)) {
            trackedKills[uuid] = KillData(kills, kills)
        } else {
            trackedKills[uuid] = KillData(trackedKills[uuid]?.startKills ?: kills, kills)
        }
    }

    override fun render(context: GuiGraphics) {
        if (!HypixelUtils.onSkyblock()) return
        if (!config.enabled) return
        if (trackedKills.isEmpty()) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphics) {
        var kills = 0
        for ((_, data) in trackedKills) {
            kills += data.currentKills - data.startKills
        }
        if (kills == 0) return
        context.drawString(
            Minecraft.getInstance().font, Utils.componentBuilder {
                append("Total kills today: ")
                appendWithColor("$kills", ChatFormatting.YELLOW)
            }, 0, 0, -1
        )
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "reset" {
                        runs {
                            Utils.sendMessageToPlayer(
                                Utils.componentBuilder {
                                    append("Reset kills today")
                                    trackedKills.clear()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun addListCommandData(): Component {
        return Utils.componentBuilder {
            append(" ")
            append {
                append("[")
                appendEmoji("repeat") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName reset"
                hover = Component.literal("Click to reset tracked kills")
                append("]")
                withColor(ChatFormatting.YELLOW)
            }
        }
    }
}

private data class KillData(
    val startKills: Int,
    val currentKills: Int,
)