package com.nopo.features.inventory

import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.ChatEvent
import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.group
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items

object RaffleQuests : FeatureModule("raffleQuestDisplay", NopoMod.config.raffleQuestConfig,
    ConfigData(
        Component.literal("Raffle Quest Display"),
        Utils.componentBuilder {
            append("Displays your Century Raffle quests on your screen")
        }
    ), shouldBeHidden = { !Utils.isDevAllowed() }), TickEvent, GuiRendering, ChatEvent {

    fun getConfig() = config as PositionConfig

    const val RAFFLE_TASKS = "Raffle Tasks"
    val questSlots = 10..34

    // RAFFLE TASK! You completed the Ender Slayer raffle task and earned +1 Raffle Ticket and a slice of cake!
    val messageRegex = Regex("RAFFLE TASK! You completed the (?<task>[\\w ]+) raffle task and earned \\+1 Raffle Ticket and a slice of cake!")

    const val RESET_MESSAGE = "Your Raffle Tasks have refreshed! Click HERE to view your new ones!"

    val tasks = mutableMapOf<String, Component>()

    override fun onTick(totalTicks: Int) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        val screen = Minecraft.getInstance().screen
        if (screen !is ContainerScreen) return
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return
        val containerTitle = screen.title.string
        if (containerTitle == RAFFLE_TASKS) {
            tasks.clear()
            for (index in questSlots) {
                val stack = slots[index].item
                if (stack.item == Items.LIGHT_GRAY_STAINED_GLASS_PANE) continue
                val lore = stack.get(DataComponents.LORE)?.lines ?: continue
                val currentQuest = Component.empty()
                for ((index, line) in lore.withIndex()) {
                    if (index < 2) continue
                    val string = line.string.cleanColor()
                    if (string.isEmpty()) continue
                    if (string == "COMPLETE") break
                    if (string == "INCOMPLETE") {
                        val taskName = stack.hoverName.string.cleanColor()
                        tasks[taskName] = currentQuest
                        break
                    }
                    currentQuest.append(" ")
                    currentQuest.append(line)
                }
            }
        }
    }

    override fun render(context: GuiGraphics) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (tasks.isEmpty()) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphics) {
        val font = Minecraft.getInstance().font
        if (tasks.isEmpty()) {
            context.drawString(font, Component.literal("Kill Ashfang"), 0, 0, -1)
            context.drawString(font, Component.literal("Call someone on your Abiphone"), 0, 10, -1)
        }
        for ((index, task) in tasks.entries.withIndex()) {
            context.drawString(font, task.value, 0, index * 10, -1)
        }
    }

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        val message = message.string.cleanColor()
        if (message == RESET_MESSAGE) {
            tasks.clear()
            tasks["new quest time!"] = Utils.componentBuilder {
                append("Talk to the raffle box to get new quests!!")
            }
        }
        if (messageRegex.matches(message)) {
            val task = messageRegex.group(message, "task") ?: return
            if (tasks.contains(task)) tasks.remove(task)
        }
    }

}