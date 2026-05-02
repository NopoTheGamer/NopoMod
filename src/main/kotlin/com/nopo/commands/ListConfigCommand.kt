package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.events.CommandRegistration
import com.nopo.module.Module
import com.nopo.utils.Utils
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import net.minecraft.network.chat.Component

object ListConfigCommand : Module("list config", needsToggle = false), CommandRegistration {
    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "list" {
                runs {
                    Utils.sendMessageToPlayer("Current Config:")
                    for (module in NopoMod.modules) {
                        if (!module.needsToggle) continue
                        if (module.dev && !Utils.isDevAllowed()) continue
                        Utils.sendMessageToPlayer(
                            componentBuilder {
                                append("${module.moduleName} ")
                                if (module.config.enabled) {
                                    append(Utils.createEmoji("white_check_mark"))
                                } else {
                                    append(Utils.createEmoji("x"))
                                }
                                command = "/nopo feature ${module.moduleName}"
                                hover = Component.literal("Click to toggle")
                            }
                        )
                    }
                }
            }
        }
    }
}