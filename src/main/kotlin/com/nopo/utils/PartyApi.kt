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

        Utils.registerDebugScreenEntry("party_data", { inParty() }) {
            add("[Nopo] Party Size: ${memberMap.size}")
        }
    }

    private var lastSendPacket = -1L
    private var lastSendMessage = -1L

    fun sendPartyMessage(message: String) {
        sendPartyPacket()
        val currentMs = System.currentTimeMillis()
        if (currentMs - lastSendMessage > 200) {
            lastSendMessage = currentMs
            DelayedRuns.schedule(5) {
                if (inParty()) Utils.sendCommandToServer("pc $message")
            }
        }
    }

    fun sendPartyPacket() {
        val currentMs = System.currentTimeMillis()
        if (currentMs - lastSendPacket > 2500) {
            lastSendPacket = currentMs
            HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())
        }
    }

    fun inParty() = isInParty

}