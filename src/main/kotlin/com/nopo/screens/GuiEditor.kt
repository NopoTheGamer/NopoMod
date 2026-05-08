package com.nopo.screens

import com.nopo.config.ConfigManager
import com.nopo.utils.Position
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

    // TODO write centered text and put some here

    override fun render(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        super.render(guiGraphics, i, j, f)
        pos.render(guiGraphics) {
            runnable(guiGraphics)
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
        ConfigManager.save()
        super.onClose()
    }
}