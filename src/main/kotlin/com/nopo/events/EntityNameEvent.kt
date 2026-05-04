package com.nopo.events

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

interface EntityNameEvent {
    fun onEntityName(entity: Player, original: Component): Component?
}