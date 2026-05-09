package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.url
import net.minecraft.network.chat.Component

object DiscordCommand : BaseModule("discord command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "discord" {
                runs {
                    Utils.sendMessageToPlayer(
                        componentBuilder {
                            append("Click here to join the discord!") {
                                url = "https://discord.com/invite/anFE6xUK6y"
                                hover = Component.literal("Click to join!")
                                withColor(7506394)
                            }
                        }
                    )
                }
            }
        }
    }
}