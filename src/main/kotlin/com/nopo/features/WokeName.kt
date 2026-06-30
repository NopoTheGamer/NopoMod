package com.nopo.features

import com.nopo.NopoMod
import com.nopo.events.ModifyChat
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.replace
import net.minecraft.network.chat.Component
import java.awt.Color

object WokeName : FeatureModule("woke", NopoMod.config.wokeConfig, shouldBeHidden = { !Utils.isDevAllowed() }),
    ModifyChat {

    val nameComp by lazy {
        componentBuilder {
            append(Utils.createGradientText(Color(85, 255, 255), Color.MAGENTA, "meowgirlemily"))
            if (Utils.isDevAllowed()) {
                append(" ")
                val trans = Utils.createEmoji("trans")
                append(trans)
                append(trans)
            }
        }
    }

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (actionBar || !config.enabled) return null
        if (!message.string.contains("Throwpo")) return null
        val comp = message.copy().replace("Throwpo", nameComp) ?: return null
        return comp
    }

}