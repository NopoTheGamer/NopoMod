package com.nopo.events

import net.minecraft.network.chat.Component

interface AllowChat {
    fun onChat(message: Component, actionBar: Boolean): Boolean
}