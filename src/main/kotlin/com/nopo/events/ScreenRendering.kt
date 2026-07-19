package com.nopo.events

import net.minecraft.client.gui.GuiGraphicsExtractor

interface ScreenRendering {
    fun renderAfterScreen(context: GuiGraphicsExtractor)
    fun doRenderAfterScreen(context: GuiGraphicsExtractor) {

    }
}