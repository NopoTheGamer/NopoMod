package com.nopo.events

import net.minecraft.client.gui.GuiGraphicsExtractor

interface GuiRendering {
    fun render(context: GuiGraphicsExtractor)
    fun doRender(context: GuiGraphicsExtractor) {

    }
}