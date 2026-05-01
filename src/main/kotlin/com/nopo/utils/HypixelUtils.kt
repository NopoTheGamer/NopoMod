package com.nopo.utils

import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.jvm.optionals.getOrNull


object HypixelUtils {

    var map: String? = null
    var mode: String? = null

    init {
        HypixelModAPI.getInstance().createHandler<ClientboundLocationPacket?>(
            ClientboundLocationPacket::class.java
        ) { packet: ClientboundLocationPacket ->
            map = packet.map.getOrNull()
            mode = packet.mode.getOrNull()
            println(packet.map)
            println(packet.mode)
            println(packet.lobbyName)
        }
    }
}