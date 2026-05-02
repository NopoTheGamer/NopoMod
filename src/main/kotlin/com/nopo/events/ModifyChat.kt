package com.nopo.events

import net.minecraft.network.chat.Component

interface ModifyChat {
    fun onModifyChat(message: Component, actionBar: Boolean): Component?
}