package com.nopo.commands

import com.nopo.NopoMod
import com.nopo.events.ChatEvent
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import net.minecraft.network.chat.Component
import kotlin.random.Random

object MeowPartyCommand : BaseModule("meowPartyCommand"), ChatEvent {

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!NopoMod.config.callPartyCommandConfig.enabled) return
        if (!message.string.startsWith("Party >")) return
        if (!message.string.contains("!meow")) return
        if (Random.nextInt(5) == 0) {
            Utils.sendCommandToServer("pc meow")
        }
    }
}