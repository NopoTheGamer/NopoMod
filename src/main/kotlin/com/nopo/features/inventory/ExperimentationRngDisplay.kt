package com.nopo.features.inventory

import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.GuiRendering
import com.nopo.events.ScreenRendering
import com.nopo.events.TickEvent
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.group
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items

object ExperimentationRngDisplay : FeatureModule("experimentationRngDisplay", NopoMod.config.experimentRngConfig), TickEvent, GuiRendering, ScreenRendering {

    fun getConfig() = config as PositionConfig

    //                          379,493/500k
    val rngRegex = Regex("\\s+(?<amount>[\\d,]+)/(?<needed>.*)")

    var rngXp: Component? = null

    override fun onTick(totalTicks: Int) {
        rngXp = null
        if (!config.enabled) return
        val screen: Screen? = Minecraft.getInstance().screen
        if (screen !is ContainerScreen) return
        val title = screen.getTitle().string
        if (!title.startsWith("Experimentation Table")) return
        val slots = Minecraft.getInstance().player!!.containerMenu.slots
        val rngItem = slots[48].item
        if (rngItem.item != Items.PLAYER_HEAD) return
        val lines = rngItem.get(DataComponents.LORE)?.lines ?: return
        for (line in lines) {
            if (!line.string.matches(rngRegex)) continue
            val amount = rngRegex.group(line.string, "amount") ?: continue
            val match = Utils.matcher(line, amount) ?: continue
            rngXp = match
        }
    }

    override fun render(context: GuiGraphics) {

    }

    override fun doRender(context: GuiGraphics) {
        doRenderAfterScreen(context)
    }

    override fun renderAfterScreen(context: GuiGraphics) {
        if (!config.enabled) return
        rngXp ?: return
        getConfig().pos.render(context) {
            doRenderAfterScreen(context)
        }
    }

    override fun doRenderAfterScreen(context: GuiGraphics) {
        context.drawString(
            Minecraft.getInstance().font, Utils.componentBuilder {
                rngXp?.let { append(it) } ?: appendWithColor("325,212", ChatFormatting.LIGHT_PURPLE)
                appendWithColor(" RNG Meter", ChatFormatting.LIGHT_PURPLE)
            }, 0, 0, -1
        )
    }
}