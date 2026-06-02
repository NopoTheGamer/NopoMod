package com.nopo.utils

import com.google.gson.annotations.Expose
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

data class Position(
    @Expose var x: Int = 100,
    @Expose var y: Int = 100,
    @Expose var scale: Float = 1f,
) {
    fun render(context: GuiGraphicsExtractor, block: () -> Unit) {
        if (Minecraft.getInstance().options.hideGui) return
        context.pose().pushMatrix()
        context.pose().translate(x.toFloat(), y.toFloat())
        if (scale != 1f) context.pose().scale(scale)
        block()
        context.pose().popMatrix()
        return
    }
}