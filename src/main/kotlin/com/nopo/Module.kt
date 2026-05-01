package com.nopo

import com.google.gson.annotations.Expose
import com.mojang.brigadier.CommandDispatcher
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.withColor
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.utils.Utils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext

open class Module(
    val name: String,
    @Expose var config: ModuleConfig = NopoMod.config.dummyConfig,
    val needsToggle: Boolean = true,
    val dev: Boolean = false
) {

    open fun onCommandRegister(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registry: CommandBuildContext
    ) {
        if (!needsToggle) return
        if (dev && !Utils.isDev()) return
        dispatcher.register(
            ClientCommandManager.literal("nopo")
                .then(ClientCommandManager.literal("toggle").then(
                ClientCommandManager.literal(name).executes {
                    config.enabled = !config.enabled
                    Utils.sendMessageToPlayer(
                        componentBuilder {
                            append("$name module ")
                            if (config.enabled) {
                                append("enabled")
                            } else {
                                append("disabled")
                            }
                            withColor(ChatFormatting.YELLOW)
                        })
                    ConfigManager.save()
                    1
                }))
        )
    }

}