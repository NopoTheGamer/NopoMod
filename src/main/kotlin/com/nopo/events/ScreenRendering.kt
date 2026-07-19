package com.nopo.events

import net.minecraft.client.gui.GuiGraphics

interface ScreenRendering {
    fun renderAfterScreen(context: GuiGraphics)
    fun doRenderAfterScreen(context: GuiGraphics) {

    }
}