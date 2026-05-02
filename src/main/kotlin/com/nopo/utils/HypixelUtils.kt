package com.nopo.utils

import com.nopo.module.Module
import com.nopo.NopoMod
import com.nopo.events.IslandChange
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.jvm.optionals.getOrNull


object HypixelUtils : Module("hypixel utils", needsToggle = false) {

    var map: String? = null
    var mode: String? = null
    var currentIsland: IslandType = IslandType.UNKNOWN

    init {
        HypixelModAPI.getInstance().createHandler<ClientboundLocationPacket?>(
            ClientboundLocationPacket::class.java
        ) { packet: ClientboundLocationPacket ->
            map = packet.map.getOrNull()
            mode = packet.mode.getOrNull()
            val prev = currentIsland
            currentIsland = IslandType.UNKNOWN
            for (island in IslandType.entries) {
                if (island.map == map && island.mode == mode) {
                    currentIsland = island
                    break
                }
            }
            NopoMod.modules.forEach {
                if (it is IslandChange) {
                    it.onWorldSwap(currentIsland, prev)
                }
            }
        }

        Utils.registerDebugScreenEntry("current_area", { true }) {
            add("[Nopo] Current Map: $map")
            add("[Nopo] Current Mode: $mode")
            add("[Nopo] Current Island: $currentIsland")
        }
    }
}