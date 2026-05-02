package com.nopo.events

import net.minecraft.network.chat.Component

interface ChatEvent {
    fun onChat(message: Component, actionBar: Boolean)
}