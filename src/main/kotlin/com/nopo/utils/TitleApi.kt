package com.nopo.utils

import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.BaseModule
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

object TitleApi : BaseModule("title api"), TickEvent, GuiRendering {

    val queue = mutableMapOf<Component, Position>()
    var ticks = 30

    fun displayTitle(title: Component, position: Position) {
        queue[title] = position
    }

    override fun onTick(totalTicks: Int) {
        if (queue.isEmpty()) return
        val data = queue.entries.first()
        ticks--
        if (ticks <= 0) {
            ticks = 60
            queue.entries.remove(data)
        }
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (queue.isEmpty()) return
        val (title, position) = queue.entries.first()
        position.render(context) {
            context.text(Minecraft.getInstance().font, title, 0, 0, -1)
        }
    }
}