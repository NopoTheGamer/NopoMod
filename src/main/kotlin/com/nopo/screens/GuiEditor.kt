package com.nopo.screens

import com.nopo.config.ConfigManager
import com.nopo.utils.Position
import com.nopo.utils.Utils
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class GuiEditor(val pos: Position, val runnable: (GuiGraphics) -> Unit) : Screen(Component.literal("Gui Editor")) {

    override fun init() {
        super.init()
    }

    var firstX = pos.x
    var firstY = pos.y
    var firstScale = pos.scale

    val tips = listOf(
        Component.literal("Gui Editor"),
        Component.literal("Click to place element"),
        Component.literal("Scroll to rescale element"),
        Component.literal("Click r to reset scale to 1"),
    )

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        super.render(context, mouseX, mouseY, f)
        for ((index, component) in tips.withIndex()) {
            Utils.drawCenteredText(context, component, 0, 10 + index * 10)
        }
        pos.render(context) {
            runnable(context)
        }
    }

    override fun mouseMoved(x: Double, y: Double) {
        super.mouseMoved(x, y)
        pos.x = x.toInt()
        pos.y = y.toInt()
    }

    override fun mouseClicked(mouseEvent: MouseButtonEvent, bl: Boolean): Boolean {
        super.mouseClicked(mouseEvent, bl)
        if (mouseEvent.button() != 0) return false
        firstX = mouseEvent.x().toInt()
        firstY = mouseEvent.y().toInt()
        firstScale = pos.scale
        onClose()
        return true
    }

    override fun charTyped(characterEvent: CharacterEvent): Boolean {
        if (characterEvent.codepointAsString() == "r") {
            pos.scale = 1f
        }
        return super.charTyped(characterEvent)
    }

    override fun mouseScrolled(d: Double, e: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY > 0) pos.scale += 0.1f
        else if (0 > scrollY) pos.scale -= 0.1f
        pos.scale = pos.scale.coerceIn(0.2f, 10f)
        return super.mouseScrolled(d, e, scrollX, scrollY)
    }

    override fun onClose() {
        pos.x = firstX
        pos.y = firstY
        pos.scale = firstScale
        ConfigManager.save()
        super.onClose()
    }
}