package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.screens.ConfigScreen
import com.nopo.utils.Utils

object ConfigSearchCommand : BaseModule("nopoc command"), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopoc") {
            executable {
                param("category").suggests {
                    Utils.getAllCategories().map { it.lowercase() }
                }
                runs { category: String ->
                    NopoMod.screenToOpen = ConfigScreen(category)
                }
            }
            runs {
                NopoMod.screenToOpen = ConfigScreen()
            }
        }
    }
}