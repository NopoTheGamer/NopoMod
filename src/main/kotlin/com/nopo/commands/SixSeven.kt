package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.Module
import com.nopo.events.CommandRegistration
import com.nopo.utils.Utils

object SixSeven : Module("sixseven", needsToggle = false), CommandRegistration {

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            literal("sixseven") {
                runs {
                    Utils.sendMessageToPlayer(Utils.componentBuilder {
                        append("really... ")
                        append(Utils.createEmoji("upside_down"))
                    })
                }
            }
        }
    }

}