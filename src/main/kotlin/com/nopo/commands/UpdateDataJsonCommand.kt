package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.Utils

object UpdateDataJsonCommand : BaseModule("upate data json command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "updateData" {
                runs {
                    NopoMod.downloadDataJson(false)
                    Utils.sendMessageToPlayer("Updated data json")
                }
            }
        }
    }
}