package com.nopo.features.rift

import com.nopo.NopoMod
import com.nopo.events.IslandChange
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.IslandType
import com.nopo.utils.TabWidget
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting

object ShensOutbidNotification : FeatureModule("riftShenOutbidNotification", NopoMod.config.riftShenOutbid), IslandChange {
    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (newIsland != IslandType.RIFT) return
        DelayedRuns.schedule(400) {
            for (line in TabWidget.SHENS.lines) {
                // have not tested this yet (lol)
                if (line.string == " Outbid") {
                    Utils.sendMessageToPlayer(
                        Utils.componentBuilder {
                            appendEmoji("rotating_light")
                            appendEmoji("rotating_light")
                            append(" Outbid on Shens Auction ") {
                                withColor(ChatFormatting.YELLOW)
                            }
                            appendEmoji("rotating_light")
                            appendEmoji("rotating_light")
                        }
                    )
                }
            }
        }
    }
}