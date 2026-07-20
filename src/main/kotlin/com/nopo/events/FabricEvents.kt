package com.nopo.events

import com.nopo.NopoMod
import com.nopo.NopoMod.MOD_ID
import com.nopo.NopoMod.modules
import com.nopo.module.BaseModule
import com.nopo.module.FeatureModule
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object FabricEvents : BaseModule("fabric events") {

    private var ticks = 0
    private var screenTicks = 0

    init {
        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            for (module in modules) {
                if (module is FeatureModule) {
                    module.registerToggleCommand()?.register(dispatcher)
                }
                if (module is CommandRegistration) {
                    module.createCommand()?.register(dispatcher)
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            if (Minecraft.getInstance().player == null) return@register
            if (Minecraft.getInstance().level == null) return@register

            if (NopoMod.screenToOpen != null) {
                screenTicks++
                if (screenTicks == 5) {
                    Minecraft.getInstance().setScreen(NopoMod.screenToOpen)
                    screenTicks = 0
                    NopoMod.screenToOpen = null
                }
            }

            val tick = ++ticks
            for (module in modules) {
                if (module is TickEvent) {
                    module.onTick(tick)
                }
            }
        }

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register { _, _ ->
            for (module in modules) {
                if (module is WorldChange) {
                    module.onWorldChange()
                }
            }
        }

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.SLEEP,
            Identifier.fromNamespaceAndPath(MOD_ID, "rendering")
        ) { context: GuiGraphicsExtractor, _: DeltaTracker ->
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

        ItemTooltipCallback.EVENT.register(ItemTooltipCallback { itemStack: ItemStack, _: Item.TooltipContext, _: TooltipFlag?, lore: MutableList<Component> ->
            for (module in modules) {
                if (module is TooltipEvent) module.onTooltip(itemStack, lore)
            }
        })
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