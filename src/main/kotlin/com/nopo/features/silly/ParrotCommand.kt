package com.nopo.features.silly

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.Utils
import net.minecraft.world.entity.animal.parrot.Parrot

object ParrotCommand : BaseModule("parrot"), CommandRegistration {

    private fun getConfig() = NopoMod.config.parrotConfig

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "parrot" {
                executable {
                    param("shoulder") {
                        suggests("left", "right")
                    }
                    param("parrotType") {
                        suggests("red_blue", "blue", "green", "yellow_blue", "gray", "none")
                    }
                    runs { shoulder: String?, parrotType: String? ->
                        if (shoulder == null || parrotType == null) {
                            Utils.sendMessageToPlayer("Do /nopo parrot <left|right> <side>")
                            return@runs
                        }
                        if (getConfig().parrots == null) getConfig().parrots = mutableMapOf()
                        val side = Shoulder.getShoulder(shoulder)
                        if (side == null) {
                            Utils.sendMessageToPlayer("Unknown Shoulder")
                            return@runs
                        }
                        val parrot =
                            Parrot.Variant.entries.firstOrNull { it.name.equals(parrotType, ignoreCase = true) }
                        getConfig().parrots!![side] = parrot
                        ConfigManager.save()
                    }
                }
            }
        }
    }
}

class ParrotConfig {
    @Expose
    var parrots: MutableMap<Shoulder, Parrot.Variant?>? = mutableMapOf()
}

enum class Shoulder {
    LEFT,
    RIGHT;

    companion object {
        fun getShoulder(string: String): Shoulder? {
            return entries.firstOrNull { it.name.equals(string, ignoreCase = true) }
        }
    }
}