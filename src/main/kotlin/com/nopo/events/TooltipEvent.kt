package com.nopo.events

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

interface TooltipEvent {
    fun onTooltip(itemStack: ItemStack, lore: MutableList<Component>)
}