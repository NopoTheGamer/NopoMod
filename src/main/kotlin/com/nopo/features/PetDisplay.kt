package com.nopo.features

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.ibm.icu.text.CompactDecimalFormat
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.Module
import com.nopo.screens.GuiEditor
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Position
import com.nopo.utils.Rarity
import com.nopo.utils.TabWidget
import com.nopo.utils.Utils
import com.nopo.utils.Utils.addSeparators
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.formatDouble
import com.nopo.utils.Utils.formatInt
import com.nopo.utils.Utils.group
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import java.util.Locale

object PetDisplay : Module("petDisplay", NopoMod.config.petDisplay), CommandRegistration, GuiRendering, TickEvent {

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
                    "chatMessage" {
                        runs {
                            Utils.sendMessageToPlayer(
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

    override fun render(context: GuiGraphics) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return

        doRender(context)
    }

    private fun doRender(context: GuiGraphics) {
        val display = display ?: return
        val font = Minecraft.getInstance().font
        for ((index, component) in display.withIndex()) {
            context.drawString(font, component, getConfig().pos.x, getConfig().pos.y + index * 10, -1)
        }
    }

    override fun onTick(totalTicks: Int) {
        if (TabWidget.PET.lines.isEmpty() || (!getConfig().enabled && !getConfig().chatMessage)) {
            display = null
            return
        }
        val temp = mutableListOf<Component>()
        var level = 100
        var overflowLevel = 100
        var name = ""
        var rarity = Rarity.UNKNOWN
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
        temp.add(1, generateCustomName(overflowLevel, level, nameComponent, name, rarity))

        if (getConfig().chatMessage && name == currentPet && currentOverflowLevel + 1 == overflowLevel && level > 100) {
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
}

class PetConfig : ModuleConfig() {
    @Expose
    var pos = Position(660, 420)

    @Expose
    var chatMessage = true
}