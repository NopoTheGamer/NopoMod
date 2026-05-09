package com.nopo.features

import com.nopo.module.FeatureModule
import com.nopo.NopoMod
import com.nopo.utils.Utils
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.replace
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import java.awt.Color

object WokeName : FeatureModule("woke", NopoMod.config.wokeConfig, dev = true) {

    val nameComp by lazy {
        componentBuilder {
            append(Utils.createGradientText( Color(85, 255, 255), Color.MAGENTA, "Throwpo"))
            // TODO: remove on 26.1
            if (Utils.isDevAllowed()) {
                append(" ")
                val trans = Utils.createEmoji("trans")
                append(trans)
                append(trans)
            }
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