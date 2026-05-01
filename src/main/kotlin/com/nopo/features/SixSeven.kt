package com.nopo.features

import com.mojang.brigadier.CommandDispatcher
import com.nopo.Module
import com.nopo.Utils
import com.nopo.Utils.componentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext

object SixSeven : Module("sixseven", needsToggle = false) {

    override fun onCommandRegister(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registry: CommandBuildContext
    ) {
        dispatcher.register(
        ClientCommandManager.literal("nopo")
            .then(
                ClientCommandManager.literal("sixseven")
                    .executes {
                        Utils.sendMessageToPlayer(componentBuilder {
                            append("really... ")
                            append(Utils.createEmoji("upside_down"))
                        })
                        1
                    }
            )
        )
    }

}