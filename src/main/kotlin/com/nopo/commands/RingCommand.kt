package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.events.CommandRegistration
import com.nopo.events.TickEvent
import com.nopo.module.BaseModule
import com.nopo.utils.Utils

object RingCommand : BaseModule("ring command"), CommandRegistration, TickEvent {

    private var ticks = 0
    private var currentPersonToAnnoy: String? = null

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            // lowkey wish aliases worked...
            "ring" {
                runs { name: String? ->
                    if (name == null) {
                        Utils.sendMessageToPlayer("You gotta ring someone :/")
                        return@runs
                    }
                    currentPersonToAnnoy = name
                }
            }
            "call" {
                runs { name: String? ->
                    if (name == null) {
                        Utils.sendMessageToPlayer("You gotta ring someone :/")
                        return@runs
                    }
                    currentPersonToAnnoy = name
                }
            }
        }
    }

    override fun onTick(totalTicks: Int) {
        if (currentPersonToAnnoy == null) {
            ticks = 0
            return
        }
        ticks++
        if (ticks == 5) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING...")
        }
        if (ticks == 17) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING... RING...")
        }
        if (ticks == 29) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING... RING... RING...")
            currentPersonToAnnoy = null
        }
    }
}