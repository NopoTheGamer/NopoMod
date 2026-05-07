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
        var name = ""
        for (line in TabWidget.PET.lines) {
            val string = line.string
            if (petNameRegex.matches(string)) {
                val levelMatch = petNameRegex.group(string, "level")?.formatInt()
                val nameMatch = petNameRegex.group(string, "name")
                if (levelMatch != null) {
                    level = levelMatch
                }
                if (nameMatch != null) {
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
                val xp = match.formatDouble().toFloat() + OverflowPetLevels.getCalculativeXpForLevel(level)
                var overflowLevel = OverflowPetLevels.calcLevel(xp)
                if (level == 200) overflowLevel--
                level = overflowLevel

                val xpComp = componentBuilder {
                    val progressXp = OverflowPetLevels.calcLeftOverXp(xp)
                    append(" ${progressXp.addSeparators()}") {
                        withColor(ChatFormatting.YELLOW)
                    }
                    append("/") {
                        withColor(ChatFormatting.GOLD)
                    }
                    val fmt = CompactDecimalFormat.getInstance(Locale.US, CompactDecimalFormat.CompactStyle.SHORT)
                    val xpForNextLevel = OverflowPetLevels.getXpForLevel(overflowLevel)
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
        temp.add(1, generateCustomName(level, nameComponent, name))
        display = temp

        if (getConfig().chatMessage && name == currentPet && currentOverflowLevel != level && currentOverflowLevel != -1) {
            Utils.sendMessageToPlayer(
                componentBuilder {
                    withColor(ChatFormatting.GREEN)
                    append("Your ")
                    append(nameComponent)
                    append(" leveled up to level ")
                    append("$level") {
                        withColor(ChatFormatting.BLUE)
                    }
                    append("!")
                }
            )
        }
        currentOverflowLevel = level
        currentPet = name
    }

    fun generateCustomName(level: Int, nameComponent: Component?, nameMatch: String): Component {
        return componentBuilder {
            append(" [Lvl $level] ") {
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
    var pos = Position()

    @Expose
    var chatMessage = true
}