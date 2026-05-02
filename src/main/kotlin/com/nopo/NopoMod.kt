package com.nopo

import com.nopo.commands.ListConfigCommand
import com.nopo.config.Config
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.features.DebugModule
import com.nopo.features.OverflowPetLevels
import com.nopo.commands.SixSeven
import com.nopo.commands.TaskList
import com.nopo.events.AllowChat
import com.nopo.events.GuiRendering
import com.nopo.events.ModifyChat
import com.nopo.events.TickEvent
import com.nopo.events.WorldChange
import com.nopo.features.AshwreathReminder
import com.nopo.features.FirstTImeGreeting
import com.nopo.features.WokeName
import com.nopo.features.emoji.EmojiReplace
import com.nopo.module.Module
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

object NopoMod : ModInitializer {

    const val MOD_ID = "nopo"
    lateinit var config: Config
    var modules: List<Module> = emptyList()
        private set
    private var ticks = 0


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
            AshwreathReminder,
            DelayedRuns,
            ListConfigCommand,
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

        ClientTickEvents.END_CLIENT_TICK.register {
            if (Minecraft.getInstance().player == null) return@register
            if (Minecraft.getInstance().level == null) return@register

            for (module in modules) {
                if (module is TickEvent) {
                    module.onTick(++ticks)
                }
            }
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            for (module in modules) {
                if (module is WorldChange) {
                    module.onWorldChange()
                }
            }
        }

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.SLEEP,
            Identifier.fromNamespaceAndPath(MOD_ID, "rendering")
        ) { context: GuiGraphics, _: DeltaTracker ->
            for (module in modules) {
                if (module is GuiRendering) {
                    module.render(context)
                }
            }
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { component, actionBar ->
            var allowed = true
            for (module in modules) {
                if (module is AllowChat) {
                    if (!module.onChat(component, actionBar)) {
                        allowed = false
                    }
                }
            }
            allowed
        }

        ClientReceiveMessageEvents.MODIFY_GAME.register { component, actionBar ->
            var newComponent: Component = component.copy()
            var hasChanged = false
            for (module in modules) {
                if (module is ModifyChat) {
                    val newComp = module.onModifyChat(newComponent, actionBar)
                    if (newComp != null) {
                        newComponent = newComp
                        hasChanged = true
                    }
                }
            }
            if (hasChanged) newComponent
            else component
        }
    }


}