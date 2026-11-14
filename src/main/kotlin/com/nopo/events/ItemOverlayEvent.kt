package com.nopo.events

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.ItemStack

interface ItemOverlayEvent {

    fun onItemOverlay(drawContext: GuiGraphicsExtractor, font: Font, stack: ItemStack, x: Int, y: Int)
}