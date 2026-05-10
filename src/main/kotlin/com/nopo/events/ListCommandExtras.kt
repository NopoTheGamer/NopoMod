package com.nopo.events

import net.minecraft.network.chat.Component

interface ListCommandExtras {
    fun addListCommandData(): Component
}