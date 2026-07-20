package com.nopo.features

import com.mojang.blaze3d.platform.InputConstants
import com.nopo.NopoMod
import com.nopo.events.ChatEvent
import com.nopo.events.TickEvent
import com.nopo.module.FeatureModule
import com.nopo.utils.SkyHanniUtils
import com.nopo.utils.Utils
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW


object AutoJoinParty : FeatureModule("autoJoinParty", NopoMod.config.autoJoinPartyConfig, null, { !Utils.isDevAllowed() && !Utils.isCal() }), ChatEvent, TickEvent {

    val sendLocation = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.nopo.send_location",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            KeyMapping.Category.MISC
        )
    )

    override fun onChat(message: Component, actionBar: Boolean) {
        if (!config.enabled) return
        val string = message.string
        if (string.contains("has invited you to join")) {
            if (string.contains("meowgirlemily")) {
                Utils.sendCommandToServer("p meowgirlemily")
            } else if (string.contains("CalMWolfs")) {
                Utils.sendCommandToServer("p CalMWolfs")
            }
        }
    }

    override fun onTick(totalTicks: Int) {
        if (!config.enabled || !SkyHanniUtils.isSkyHanniLoaded) return
        if (Minecraft.getInstance().player == null) return
        val pos = SkyHanniUtils.getTargetedBlock() ?: return
        while (sendLocation.consumeClick()) {
            Utils.sendCommandToServer("cc x: ${pos.x}, y: ${pos.y}, z: ${pos.z}")
        }
    }
}