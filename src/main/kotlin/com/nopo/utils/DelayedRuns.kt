package com.nopo.utils

import com.nopo.events.TickEvent
import com.nopo.module.Module
import net.minecraft.client.Minecraft

object DelayedRuns : Module("delayed run", needsToggle = false), TickEvent {

    val map = mutableListOf<Pair<Runnable, Int>>()
    var currentTicks = 0

    fun schedule(ticks: Int, runnable: Runnable) {
        map.add(runnable to ticks + currentTicks)
    }

    override fun onTick(totalTicks: Int) {
        currentTicks = totalTicks
        map.removeIf {
            if (currentTicks > it.second) {
                Minecraft.getInstance().schedule(it.first)
                true
            } else {
                false
            }
        }
    }

}