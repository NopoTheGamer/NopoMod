package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object MainCommand : BaseModule("nopo command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            runs {
                Utils.sendMessageToPlayer(
                    componentBuilder {
                        append("If you are looking for the config menu do ")
                        append("/nopo list ") {
                            withColor(ChatFormatting.YELLOW)
                            command = "/nopo list"
                            hover = Component.literal("Click to run command!")
                        }
                        append("and click on the stuff in chat to toggle stuff on or off. Do ")
                        append("/nopo feature <featureName>") {
                            withColor(ChatFormatting.YELLOW)
                            suggest = "/nopo feature "
                            hover = Component.literal("Click to put command in chatbox!")
                        }
                        append(" to quickly toggle individual features, some commands also have sub commands!")
                    }
                )
            }
        }
    }
}