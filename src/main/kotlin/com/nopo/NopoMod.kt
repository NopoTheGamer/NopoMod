package com.nopo

import com.nopo.config.Config
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.features.DebugModule
import com.nopo.features.OverflowPetLevels
import com.nopo.commands.SixSeven
import com.nopo.commands.TaskList
import com.nopo.features.FirstTImeGreeting
import com.nopo.features.WokeName
import com.nopo.features.emoji.EmojiReplace
import com.nopo.module.Module
import com.nopo.utils.HypixelUtils
import net.fabricmc.api.ModInitializer
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
            EmojiReplace,
            DebugModule,
            HypixelUtils,
            OverflowPetLevels,
            TaskList,
            FirstTImeGreeting,
        )

        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            for (module in modules) {
                module.registerToggleCommand()?.register(dispatcher)
                if (module is CommandRegistration) {
                    module.createCommand().register(dispatcher)
                }
            }
        }
    }


}