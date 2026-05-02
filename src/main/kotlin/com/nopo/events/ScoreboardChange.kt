package com.nopo.events

import net.minecraft.network.chat.Component

interface ScoreboardChange {
    fun onScoreboardChange(
        added: List<Component>,
        removed: List<Component>,
        new: List<Component>,
        old: List<Component>,
    )
}