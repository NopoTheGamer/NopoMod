package com.nopo.features

import com.nopo.Module
import com.nopo.NopoMod
import com.nopo.Utils
import com.nopo.Utils.componentBuilder
import com.nopo.Utils.replace
import com.nopo.config.ModuleConfig
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import java.awt.Color

object WokeName : Module("woke", NopoMod.config.wokeConfig, dev = true) {

    val nameComp by lazy {
        componentBuilder {
            append(Utils.createGradientText( Color(85, 255, 255), Color.MAGENTA, "Throwpo"))
            append(" ")
            val trans = Utils.createEmoji("trans")
            append(trans)
            append(trans)
        }
    }

    init {
        ClientReceiveMessageEvents.MODIFY_GAME.register { component, actionBar ->
            if (actionBar || !config.enabled) return@register component
            if (!component.string.contains("Throwpo")) return@register component
            val comp = component.copy().replace("Throwpo", nameComp) ?: return@register component
            return@register comp
        }
    }
}

class WokeConfig: ModuleConfig()