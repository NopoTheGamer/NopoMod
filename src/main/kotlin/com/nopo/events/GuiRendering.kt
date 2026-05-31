package com.nopo.events

import net.minecraft.client.gui.GuiGraphics

interface GuiRendering {
    fun render(context: GuiGraphics)
    fun doRender(context: GuiGraphics) {

    }
}