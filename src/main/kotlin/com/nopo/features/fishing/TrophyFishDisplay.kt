package com.nopo.features.fishing

import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.TabWidget
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.formatInt
import com.nopo.utils.Utils.group
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional

object TrophyFishDisplay : FeatureModule(
    "trophyFishDisplay", NopoMod.config.trophyFishConfig, ConfigData(
        Component.literal("Trophy Frog And Fish Display"),
        Component.literal("Requires Trophy Tab Widget. Highly recommended to turn on \"Show Total Caught\" in the /tab settings.")
    )
), GuiRendering, TickEvent {

    private fun getConfig() = config as PositionConfig

    var display: List<Component>? = mutableListOf()

    val trophyRegex = Regex(" [●○]{4} (?<name>[a-zA-Z ]+)(?: \\((?<count>[\\d,]+)\\))?")

    override fun render(context: GuiGraphics) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return

        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphics) {
        val display = display ?: return
        val font = Minecraft.getInstance().font
        for ((index, component) in display.withIndex()) {
            context.drawString(font, component, 0, index * 10, -1)
        }
    }

    override fun onTick(totalTicks: Int) {
        val lines = TabWidget.TROPHY.lines
        if (lines.size < 2 || !getConfig().enabled) {
            display = null
            return
        }
        val temp = mutableListOf<Component>()

        var anyBronzeMissing = false
        var anySilverMissing = false
        var anyGoldMissing = false

        for (line in lines) {
            if (!line.string.matches(trophyRegex)) continue

            val raritiesFound = mutableMapOf(
                RARITY.BRONZE to false,
                RARITY.SILVER to false,
                RARITY.GOLD to false,
                RARITY.DIAMOND to false,
            )

            val name = trophyRegex.group(line.string, "name")?.trim() ?: "unknown"

            line.visit({ style: Style, string: String ->
                if (string.trim() == "●") {
                    val colour = style.color?.name
                    when (colour) {
                        "aqua" -> raritiesFound[RARITY.DIAMOND] = true
                        "gold" -> raritiesFound[RARITY.GOLD] = true
                        "gray" -> raritiesFound[RARITY.SILVER] = true
                        "dark_gray" -> raritiesFound[RARITY.BRONZE] = true
                    }
                }
                if (string.trim() == "○") {
                    val colour = style.color?.name
                    when (colour) {
                        "gold" -> anyGoldMissing = true
                        "gray" -> anySilverMissing = true
                        "dark_gray" -> anyBronzeMissing = true
                    }
                }
                Optional.empty()
            }, Style.EMPTY)

            if (raritiesFound[RARITY.DIAMOND] == true) continue

            temp += Utils.componentBuilder {
                append(" ")
                if (raritiesFound[RARITY.BRONZE] != true) {
                    append(Utils.createItem("coal"))
                } else {
                    append("  ")
                }
                if (raritiesFound[RARITY.SILVER] != true) {
                    append(Utils.createItem("iron_ingot"))
                } else {
                    append("  ")
                }
                if (raritiesFound[RARITY.GOLD] != true) {
                    append(Utils.createItem("gold_ingot"))
                } else {
                    append("  ")
                }
                if (raritiesFound[RARITY.DIAMOND] != true) {
                    append(Utils.createItem("diamond"))
                } else {
                    append("  ")
                }
                append(Utils.matcherOrString(line, name))
                val count = trophyRegex.group(line.string, "count")?.formatInt()
                if (count != null) {
                    appendWithColor(" $count/${getPity(name)}", ChatFormatting.GRAY)
                }
            }
        }

        // scuffed to make the title not be weird offset
        temp.add(0, Utils.componentBuilder {
            if (!anyBronzeMissing) append("  ")
            if (!anySilverMissing) append("  ")
            if (!anyGoldMissing) append("  ")
            append(lines.first())
        })

        display = temp
    }

    private fun getPity(name: String): Int {
        if (name == "Exploding Frog") return 300
        if (name == "Puddle Jumper") return 300
        return 600
    }

    private enum class RARITY {
        BRONZE,
        SILVER,
        GOLD,
        DIAMOND,
    }

}