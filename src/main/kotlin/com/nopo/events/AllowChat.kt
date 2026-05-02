package com.nopo.events

import net.minecraft.network.chat.Component

interface AllowChat {
    fun onAllowChat(message: Component, actionBar: Boolean): Boolean
}