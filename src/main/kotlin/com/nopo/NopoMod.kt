package com.nopo

import com.google.gson.stream.JsonReader
import com.nopo.commands.ListConfigCommand
import com.nopo.commands.SixSeven
import com.nopo.commands.TaskList
import com.nopo.config.Config
import com.nopo.config.ConfigManager
import com.nopo.events.AllowChat
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.EntityNameEvent
import com.nopo.events.GuiRendering
import com.nopo.events.ModifyChat
import com.nopo.events.TickEvent
import com.nopo.events.WorldChange
import com.nopo.features.garden.AshwreathReminder
import com.nopo.features.DebugModule
import com.nopo.features.EquipmentDisplay
import com.nopo.features.FirstTImeGreeting
import com.nopo.features.OverflowPetLevels
import com.nopo.features.PartyFinderKickButton
import com.nopo.features.WokeName
import com.nopo.features.emoji.EmojiName
import com.nopo.features.emoji.EmojiReplace
import com.nopo.features.garden.RareCropTracker
import com.nopo.features.slayer.BossesSinceDrop
import com.nopo.module.Module
import com.nopo.utils.Data
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
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
import net.minecraft.world.entity.player.Player
import java.io.StringReader
import java.net.URI
import java.net.URL

object NopoMod : ModInitializer {

    const val MOD_ID = "nopo"
    lateinit var config: Config
    var modules: List<Module> = emptyList()
        private set
    private var ticks = 0
    private const val DATA_JSON = "https://raw.githubusercontent.com/NopoTheGamer/NopoMod/refs/heads/master/src/main/resources/assets/nopo/data.json"
    var data: Data? = null


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
            BossesSinceDrop,
            RareCropTracker,
            PartyFinderKickButton,
            EmojiName,
            EquipmentDisplay,
        )

        if (config.useLocalJson != true) {
            try {
                val json = URL.of(URI.create(DATA_JSON), null).readText()
                data = ConfigManager.gson.fromJson<Data>(JsonReader(StringReader(json)), Data::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (data == null) data = Utils.getJsonFromJar<Data>("data.json")

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
                if (module is ChatEvent) {
                    module.onChat(component, actionBar)
                }
                if (module is AllowChat) {
                    if (!module.onAllowChat(component, actionBar)) {
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

    @JvmStatic
    fun postEntityEvent(entity: Player, original: Component): Component? {
        var newComponent: Component = original.copy()
        var hasChanged = false
        for (module in modules) {
            if (module is EntityNameEvent) {
                val newComp = module.onEntityName(entity, newComponent)
                if (newComp != null) {
                    newComponent = newComp
                    hasChanged = true
                }
            }
        }
        if (hasChanged) return newComponent
        else return null
    }


}