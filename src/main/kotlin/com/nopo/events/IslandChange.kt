package com.nopo.events

import com.nopo.utils.IslandType

interface IslandChange {

    fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType)
}