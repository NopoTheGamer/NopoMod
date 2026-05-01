package com.nopo

import com.nopo.config.Config
import com.nopo.config.ConfigManager
import com.nopo.features.SixSeven
import com.nopo.features.WokeName
import com.nopo.features.emoji.EmojiReplace
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket

object NopoMod : ModInitializer {

    const val MOD_ID = "nopo"
    lateinit var config: Config
    var modules: List<Module> = emptyList()
        private set


    override fun onInitialize() {
        config = ConfigManager.init()
        ConfigManager.save()
        modules = listOf(
            WokeName,
            SixSeven,
            EmojiReplace
        )
        HypixelUtils

        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registry ->
            dispatcher.register(ClientCommandManager.literal("nopo").executes {
                Utils.sendMessageToPlayer("hi")
                1
            })
            modules.forEach { it.onCommandRegister(dispatcher, registry) }
        }
    }


}