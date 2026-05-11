package com.nopo.features.meta

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.data.Version
import com.nopo.events.SkyblockFirstJoin
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.url
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object UpdateNotification : BaseModule("update notification"), SkyblockFirstJoin {
    override fun onSkyblockFirstJoin() {
        val updateNotification = NopoMod.data?.updateNotification ?: return
        val latestVersion = updateNotification.latestVersion ?: return
        val downloadLink = updateNotification.download
        if (latestVersion > NopoMod.CURRENT_VERSION) {
            Utils.sendMessageToPlayer(
                Utils.componentBuilder {
                    append("Your mod is outdated ")
                    appendEmoji("face_with_raised_eyebrow")
                    append(" (") {
                        withColor(ChatFormatting.GRAY)
                    }
                    append("${NopoMod.CURRENT_VERSION}") {
                        withColor(ChatFormatting.RED)
                    }
                    append(") ") {
                        withColor(ChatFormatting.GRAY)
                    }
                    appendEmoji("arrow_right")
                    append(" (") {
                        withColor(ChatFormatting.GRAY)
                    }
                    append("$latestVersion") {
                        withColor(ChatFormatting.GREEN)
                    }
                    append(") ") {
                        withColor(ChatFormatting.GRAY)
                    }
                    if (downloadLink != null) {
                        append("Click here to update!") {
                            withColor(ChatFormatting.YELLOW)
                        }
                        url = downloadLink
                        hover = Component.literal("Click to open the download link in the browser")
                    }
                }
            )
        }
    }
}

data class UpdateNotificationData(
    @Expose
    var latestVersion: Version?,
    @Expose
    var download: String?,
)
