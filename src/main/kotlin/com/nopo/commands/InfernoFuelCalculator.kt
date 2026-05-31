package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.events.CommandRegistration
import com.nopo.module.BaseModule
import com.nopo.utils.SkyHanniUtils
import com.nopo.utils.Utils

object InfernoFuelCalculator : BaseModule("inferno fuel", { !SkyHanniUtils.isSkyHanniLoaded }), CommandRegistration {

    const val COAL_NEEDED = 640 + 7680 + 184320
    const val SULPHUR_NEEDED = 40 + 480 + 11520
    const val CRUDE_GABAGOOL_NEEDED = 6912

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "infernoFuel" {
                runs { veryCrudeGabagoolCount: Int? ->
                    val veryCrudeGabagoolCount = veryCrudeGabagoolCount ?: 0

                    val distillate = SkyHanniUtils.getAmountInSack("CRUDE_GABAGOOL_DISTILLATE")
                    val sulphuricCoal = SkyHanniUtils.getAmountInSack("SULPHURIC_COAL")
                    val coal = SkyHanniUtils.getAmountInSack("ENCHANTED_COAL") * 160 + SkyHanniUtils.getAmountInSack("COAL") + (sulphuricCoal * 16 * 160)
                    val sulphur = SkyHanniUtils.getAmountInSack("ENCHANTED_SULPHUR") * 160 + SkyHanniUtils.getAmountInSack("SULPHUR_ORE") + sulphuricCoal * 160

                    val craftableWithCoal = coal / COAL_NEEDED
                    val craftableWithSulphur = sulphur / SULPHUR_NEEDED
                    val craftableWithDistillate = distillate / 6
                    val craftableWithGabagool = veryCrudeGabagoolCount * 192 / CRUDE_GABAGOOL_NEEDED
                    val list = mutableListOf(craftableWithCoal, craftableWithSulphur, craftableWithDistillate)
                    if (veryCrudeGabagoolCount != 0) list += craftableWithGabagool
                    Utils.sendMessageToPlayer(
                        Utils.componentBuilder {
                            append("Fuels Craftable: ${list.min()}\n")
                            append("Coal: $craftableWithCoal\n")
                            append("Sulphur: $craftableWithSulphur\n")
                            append("Distillate: $craftableWithDistillate")
                            if (veryCrudeGabagoolCount != 0) append("\nGabagool: $craftableWithGabagool")
                        }
                    )
                }
            }
        }
    }
}