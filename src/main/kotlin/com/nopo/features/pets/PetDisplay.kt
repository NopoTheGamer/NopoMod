package com.nopo.features.pets

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.ibm.icu.text.CompactDecimalFormat
import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.ListCommandExtras
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Rarity
import com.nopo.utils.TabWidget
import com.nopo.utils.Utils
import com.nopo.utils.Utils.addSeparators
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.bold
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.formatDouble
import com.nopo.utils.Utils.formatInt
import com.nopo.utils.Utils.group
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import java.util.Locale

object PetDisplay : FeatureModule("petDisplay", NopoMod.config.petDisplay,
    ConfigData(
        Component.literal("Pet Display"),
        componentBuilder {
            append("Hud element that shows your currently selected pet\n")
            append("Shows overflow levels and progress to level 100 legendary on pets lower than legendary")
        }
    )), CommandRegistration, GuiRendering, TickEvent, ListCommandExtras {

    private fun getConfig() = config as PetConfig
    private var display: List<Component>? = null
    private val overflowXpRegex = Regex(" +\\+(?<xp>[\\d,.]+) XP")
    private val petNameRegex = Regex(" +\\[Lvl (?<level>\\d+)] (?<name>.*)")

    private var currentPet = ""
    private var currentOverflowLevel = -1

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                "petDisplay" {
                    "chatMessage" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Toggled overflow level up messages ")
                                    getConfig().chatMessage = !getConfig().chatMessage
                                    if (getConfig().chatMessage) {
                                        append("on")
                                    } else {
                                        append("off")
                                    }
                                    withColor(ChatFormatting.YELLOW)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        display ?: return

        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphicsExtractor) {
        val font = Minecraft.getInstance().font
        val display = display
        if (display == null) {
            val line1 = componentBuilder {
                appendWithColor("Pet:", ChatFormatting.YELLOW) {
                    bold = true
                }
            }
            val line2 = componentBuilder {
                append(
                    generateCustomName(
                        231, 200,
                        componentBuilder {
                            appendWithColor("Golden Dragon", ChatFormatting.GOLD)
                        },
                        "Golden Dragon", Rarity.LEGENDARY
                    )
                )
            }
            val line3 = componentBuilder {
                appendWithColor(" 1,832,110.4", ChatFormatting.YELLOW)
                appendWithColor("/", ChatFormatting.GOLD)
                appendWithColor("1.9M XP ", ChatFormatting.YELLOW)
                appendWithColor("(97.1%)", ChatFormatting.GOLD)
            }
            context.text(font, line1, 0, 0, -1)
            context.text(font, line2, 0, 10, -1)
            context.text(font, line3, 0, 20, -1)
            return
        }
        for ((index, component) in display.withIndex()) {
            context.text(font, component, 0, index * 10, -1)
        }
    }

    override fun onTick(totalTicks: Int) {
        if (TabWidget.PET.lines.size < 2 || (!getConfig().enabled && !getConfig().chatMessage)) {
            display = null
            return
        }
        val temp = mutableListOf<Component>()
        var level = -1
        var overflowLevel = -1
        var name = ""
        var rarity = Rarity.UNKNOWN
        var maxLevel = false
        for (line in TabWidget.PET.lines) {
            val string = line.string
            if (petNameRegex.matches(string)) {
                val levelMatch = petNameRegex.group(string, "level")?.formatInt()
                val nameMatch = petNameRegex.group(string, "name")
                if (levelMatch != null) {
                    level = levelMatch
                    overflowLevel = level
                }
                if (nameMatch != null) {
                    rarity = Rarity.getRarityByComponent(line, nameMatch.replace("✦", "").trim())
                    name = nameMatch
                    continue
                }
                temp.add(line)
                continue
            }
            if (overflowXpRegex.matches(string)) {
                maxLevel = true
                val match = overflowXpRegex.group(string, "xp")
                if (match == null) {
                    temp.add(line)
                    continue
                }
                val xp = match.formatDouble().toFloat() + OverflowPetLevels.getCalculativeXpForLevel(level, rarity)
                overflowLevel = OverflowPetLevels.calcLevel(xp)
                if (level == 200) overflowLevel--

                val xpComp = componentBuilder {
                    val progressXp = OverflowPetLevels.calcLeftOverXp(xp)
                    append(" ${progressXp.addSeparators()}") {
                        withColor(ChatFormatting.YELLOW)
                    }
                    append("/") {
                        withColor(ChatFormatting.GOLD)
                    }
                    val fmt = CompactDecimalFormat.getInstance(Locale.US, CompactDecimalFormat.CompactStyle.SHORT)
                    // holy shit this is becoming spegetti
                    val offset = if (rarity < Rarity.LEGENDARY && overflowLevel < 100) 1 else 0
                    val xpForNextLevel = OverflowPetLevels.getXpForLevel(overflowLevel - offset)
                    append("${fmt.format(xpForNextLevel)} XP") {
                        withColor(ChatFormatting.YELLOW)
                    }
                    val percent = progressXp / xpForNextLevel * 100
                    append(" (${percent.addSeparators()}%)") {
                        withColor(ChatFormatting.GOLD)
                    }
                }
                temp.add(xpComp)
                continue
            }
            temp.add(line)
        }


        val nameComponent = Utils.matcher(TabWidget.PET.lines[1], name) ?: componentBuilder {
            append(name) {
                withColor(ChatFormatting.RED)
            }
        }
        if (level != -1) {
            temp.add(1, generateCustomName(overflowLevel, level, nameComponent, name, rarity))
        }

        if (getConfig().chatMessage && name == currentPet && currentOverflowLevel + 1 == overflowLevel && maxLevel) {
            Utils.sendMessageToPlayer(
                componentBuilder {
                    withColor(ChatFormatting.GREEN)
                    append("Your ")
                    append(nameComponent)
                    append(" leveled up to level ")
                    append("$overflowLevel") {
                        withColor(ChatFormatting.BLUE)
                    }
                    append("!")
                }
            )

        }
        display = temp

        currentOverflowLevel = overflowLevel
        currentPet = name
    }

    fun generateCustomName(overflowLevel: Int, realLevel: Int, nameComponent: Component?, nameMatch: String, rarity: Rarity): Component {
        return componentBuilder {
            append {
                append(" [Lvl $overflowLevel")
                if (rarity < Rarity.LEGENDARY && realLevel != overflowLevel && overflowLevel < 100) {
                    append(" ($realLevel)")
                }
                append("] ")
                withColor(ChatFormatting.GRAY)
            }
            if (nameComponent != null) {
                append(nameComponent)
            } else {
                append(nameMatch) {
                    withColor(ChatFormatting.RED)
                }
            }
        }
    }

    override fun addListCommandData(): Component {
        return componentBuilder {
            append(" ")
            append {
                append("[")
                appendEmoji("speech_balloon") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName chatMessage"
                hover = componentBuilder {
                    append("Click to toggle sending level up messages\n")
                    append("i.e. ")
                    append(Utils.chatPrefix)
                    append {
                        withColor(ChatFormatting.GREEN)
                        append("Your ")
                        append("Golden Dragon") {
                            withColor(ChatFormatting.GOLD)
                        }
                        append(" leveled up to level ")
                        append("257") {
                            withColor(ChatFormatting.BLUE)
                        }
                        append("!")
                    }
                }
                append("]")
                if (getConfig().chatMessage) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }
}

class PetConfig : PositionConfig(660, 420) {
    @Expose
    var chatMessage = true
}