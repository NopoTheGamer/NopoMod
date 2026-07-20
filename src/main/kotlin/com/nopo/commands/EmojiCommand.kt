package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.events.CommandRegistration
import com.nopo.features.emoji.EmojiReplace
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.hover
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object EmojiCommand : BaseModule("emoji command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "emoji" {
                runs {
                    Utils.sendMessageToPlayer("Here are all the custom emojis")
                    Utils.sendMessageToPlayer("Do :emoji: in chat to send it! (Hover the emoji to see its name)")
                    if (!NopoMod.config.chatEmojis.enabled) {
                        Utils.sendMessageToPlayer(
                            Utils.componentBuilder {
                                appendEmoji("rotating_light")
                                appendWithColor(" You have chat emojis off. Do ", ChatFormatting.RED)
                                appendWithColor("/nopo feature chatEmojis", ChatFormatting.YELLOW)
                                appendWithColor(" to enable them", ChatFormatting.RED)
                                hover = Component.literal("Click to enable chat emojis")
                                command = "/nopo feature chatEmojis"
                            }
                        )
                    }
                    Utils.sendMessageToPlayer(
                        Utils.componentBuilder {
                            val emojis = EmojiReplace.emojis
                            val lastEmoji = emojis.indexOfLast { it.name == "rightwards_pushing_hand_tone5" } + 1
                            for (emoji in emojis.drop(lastEmoji)) {
                                appendEmoji(emoji.name) {
                                    hover = Component.literal(emoji.name)
                                }
                                append(" ")
                            }
                        }
                    )
                }
            }
        }
    }
}