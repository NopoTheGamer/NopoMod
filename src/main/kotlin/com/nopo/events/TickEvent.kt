package com.nopo.events

interface TickEvent {
    fun onTick(totalTicks: Int)
}