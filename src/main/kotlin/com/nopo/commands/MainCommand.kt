package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.screens.ConfigScreen

object MainCommand : BaseModule("nopo command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            runs {
                NopoMod.screenToOpen = ConfigScreen()
            }
        }
    }
}