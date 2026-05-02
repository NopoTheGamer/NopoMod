package com.nopo.features

import com.nopo.module.Module
import com.nopo.NopoMod
import com.nopo.events.IslandChange
import com.nopo.utils.IslandType
import com.nopo.utils.Utils

object DebugModule : Module("debug", NopoMod.config.debug, dev = true), IslandChange {

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (!config.enabled) return
        Utils.sendMessageToPlayer("new $newIsland old $oldIsland")
    }

}