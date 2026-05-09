package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji

object SixSeven : BaseModule("sixseven"), CommandRegistration {

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            literal("sixseven") {
                runs {
                    Utils.sendMessageToPlayer(Utils.componentBuilder {
                        append("really... ")
                        appendEmoji("upside_down")
                    })
                }
            }
        }
    }

}