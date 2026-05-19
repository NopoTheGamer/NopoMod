package com.nopo.utils

import com.nopo.module.BaseModule
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import java.util.UUID

object PartyApi : BaseModule("party api") {

    private var isInParty = false

    var memberMap: Map<UUID, ClientboundPartyInfoPacket.PartyMember> = emptyMap()
        private set

    init {
        HypixelModAPI.getInstance().createHandler<ClientboundPartyInfoPacket?>(
            ClientboundPartyInfoPacket::class.java
        ) { packet: ClientboundPartyInfoPacket ->
            isInParty = packet.isInParty
            memberMap = packet.memberMap
        }

        Utils.registerDebugScreenEntry("party_data", { true }) {
            add("[Nopo] In Party: $isInParty")
            add("[Nopo] Party Size: ${memberMap.size}")
        }
    }

    private var lastSend = -1L

    fun sendPartyMessage(message: String) {
        sendPartyPacket()
        DelayedRuns.schedule(30) {
            if (inParty()) Utils.sendCommandToServer("pc $message")
            else Utils.debug("not in party")
        }
    }

    fun sendPartyPacket() {
        val currentMs = System.currentTimeMillis()
        if (currentMs - lastSend > 2500) {
            lastSend = currentMs
            HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())
        }
    }

    fun inParty() = isInParty

}