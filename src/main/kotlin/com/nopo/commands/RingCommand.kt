package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.nopo.NopoMod
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.SkyHanniUtils
import com.nopo.utils.Utils
import net.minecraft.network.chat.Component

object RingCommand : FeatureModule("ringPartyCommand", NopoMod.config.callPartyCommandConfig,
    ConfigData(
        Component.literal("!call Party Command"),
        Component.literal("Allows your party to trigger the /nopo call command on a victim")
    )
), CommandRegistration, TickEvent, ChatEvent {

    private var ticks = 0
    private var currentPersonToAnnoy: String? = null
    private var bonusMessage: String? = null
    private val abortMessages = listOf(
        "You cannot say the same message twice!",
        "You can only send a message once every half second!",
        "Can't find a player by the name of",
        "That player is not online!",
        "You cannot message this player.",
        "That player blocked you!"
    )

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            // lowkey wish aliases worked...
            literal("ring").executable {
                param("name").suggests {
                    SkyHanniUtils.getFriends().map { it.lowercase() }
                }
                runs { name: String?, messageToSendAfter: GreedyString? ->
                    if (name == null) {
                        Utils.sendMessageToPlayer("You gotta ring someone :/")
                        return@runs
                    }
                    currentPersonToAnnoy = name
                    bonusMessage = messageToSendAfter?.string
                }
            }
            literal("call").executable {
                param("name").suggests {
                    SkyHanniUtils.getFriends().map { it.lowercase() }
                }
                runs { name: String?, messageToSendAfter: GreedyString? ->
                    if (name == null) {
                        Utils.sendMessageToPlayer("You gotta ring someone :/")
                        return@runs
                    }
                    currentPersonToAnnoy = name
                    bonusMessage = messageToSendAfter?.string
                }
            }
        }
    }

    override fun onTick(totalTicks: Int) {
        partyCooldown--
        if (currentPersonToAnnoy == null) {
            ticks = 0
            return
        }
        ticks++
        if (ticks == 5) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING...")
        }
        if (ticks == 19) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING... RING...")
        }
        if (ticks == 33) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy ✆ RING... RING... RING...")
            if (bonusMessage == null) currentPersonToAnnoy = null
        }
        if (ticks == 48 && bonusMessage != null) {
            Utils.sendCommandToServer("w $currentPersonToAnnoy $bonusMessage")
            currentPersonToAnnoy = null
            bonusMessage = null
        }
    }

    var partyCooldown = -1

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        val caller = Utils.getPartyCommand(message, "!call")
        if (caller != null && !caller.contains(" ") && config.enabled && 0 > partyCooldown) {
            partyCooldown = 2400
            currentPersonToAnnoy = caller
        }
        for (badMessage in abortMessages) {
            if (message.string.startsWith(badMessage)) currentPersonToAnnoy = null
        }
    }
}