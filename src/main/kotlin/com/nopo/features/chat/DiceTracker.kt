package com.nopo.features.chat

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.addSeparators
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.group
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object DiceTracker : FeatureModule(
    "diceTracker", NopoMod.config.diceTrackerConfig,
    ConfigData(
        Component.literal("Dice Tracker"),
        Component.literal("Do /nopo dice to see your lifetime dice stats")
    )
), ChatEvent, CommandRegistration {

    private fun getConfig() = config as DiceConfig

    // Your High Class Archfiend Dice rolled a 6! Nice! Bonus: +300
    private val regex = Regex("Your (?<type>(?:High Class )?Archfiend) Dice rolled a (?<number>\\d+)!.*")

    private const val DICE = "Archfiend"
    private const val HIGH_CLASS = "High Class Archfiend"
    private const val DICE_COST = 666_000L
    private const val DICE_PROFIT = 15_000_000L
    private const val HIGH_CLASS_COST = 6_600_000L
    private const val HIGH_CLASS_PROFIT = 100_000_000L

    private fun addRoll(highClass: Boolean) {
        if (highClass) {
            getConfig().highClassRolls++
            getConfig().highClassRollsSinceDrop++
        } else {
            getConfig().diceRolls++
            getConfig().diceRollsSinceDrop++
        }
    }

    private fun addWin(highClass: Boolean) {
        DelayedRuns.schedule(1) {
            updateWinStats(highClass, false)
            if (highClass) {
                getConfig().highClassWins++
                getConfig().highClassRollsSinceDrop = 0
            } else {
                getConfig().diceWins++
                getConfig().diceRollsSinceDrop = 0
            }
        }
    }

    private fun addDye(highClass: Boolean) {
        DelayedRuns.schedule(1) {
            updateWinStats(highClass, true)
            if (highClass) {
                getConfig().highClassDyes++
                getConfig().highClassRollsSinceDrop = 0
            } else {
                getConfig().diceDyes++
                getConfig().diceRollsSinceDrop = 0
            }
        }
    }

    private fun updateWinStats(highClass: Boolean, dye: Boolean) {
        val highestSinceDrop = getHighestSinceDrop(highClass)
        val lowestSinceDrop = getLowestSinceDrop(highClass)
        val rollsSinceDrop = getRollsSinceDrop(highClass)
        val type = if (highClass) HIGH_CLASS else DICE
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                append("You took $rollsSinceDrop ")
                append(type) {
                    if (highClass) withColor(ChatFormatting.GOLD)
                    else withColor(ChatFormatting.RED)
                }
                append(" roll")
                if (rollsSinceDrop != 1) append("s")
                val cost = if (highClass) rollsSinceDrop * HIGH_CLASS_COST else rollsSinceDrop * DICE_COST
                val profit = if (dye) 0 else if (highClass) HIGH_CLASS_PROFIT else DICE_PROFIT
                if (profit - cost < 0) {
                    append(" and")
                    appendWithColor(" ${(profit - cost).addSeparators()} coins", ChatFormatting.RED)
                }
                append(" to roll a ")
                if (dye) appendWithColor("7", ChatFormatting.DARK_PURPLE)
                else appendWithColor("6", ChatFormatting.DARK_PURPLE)
                if (profit - cost > 0) {
                    append(" and gain ")
                    appendWithColor("${(profit - cost).addSeparators()} coins", ChatFormatting.GOLD)
                }
                append(".")
                withColor(ChatFormatting.YELLOW)
            }
        )
        if (rollsSinceDrop > highestSinceDrop) {
            setHighestSinceDrop(highClass, rollsSinceDrop)
            if (highestSinceDrop != lowestSinceDrop && lowestSinceDrop != 0) {
                Utils.sendMessageToPlayer(
                    Utils.componentBuilder {
                        append("This dry streak was ${rollsSinceDrop - highestSinceDrop} rolls bigger than your previous record")
                        withColor(ChatFormatting.YELLOW)
                    }
                )
            }
        }
        if (rollsSinceDrop < lowestSinceDrop || lowestSinceDrop == 0) {
            setLowestSinceDrop(highClass, rollsSinceDrop)
            if (lowestSinceDrop != 0) {
                Utils.sendMessageToPlayer(
                    Utils.componentBuilder {
                        append("This beats your previous lowest roll count by ${lowestSinceDrop - rollsSinceDrop}")
                        withColor(ChatFormatting.YELLOW)
                    }
                )
            }
        }
    }

    private fun getHighestSinceDrop(highClass: Boolean): Int {
        return if (highClass) getConfig().highestHighClassRollSinceDrop
        else getConfig().highestDiceRollSinceDrop
    }

    private fun setHighestSinceDrop(highClass: Boolean, value: Int) {
        if (highClass) getConfig().highestHighClassRollSinceDrop = value
        else getConfig().highestDiceRollSinceDrop = value
    }

    private fun getLowestSinceDrop(highClass: Boolean): Int {
        return if (highClass) getConfig().lowestHighClassRollSinceDrop
        else getConfig().lowestDiceRollSinceDrop
    }

    private fun setLowestSinceDrop(highClass: Boolean, value: Int) {
        if (highClass) getConfig().lowestHighClassRollSinceDrop = value
        else getConfig().lowestDiceRollSinceDrop = value
    }

    private fun getRollsSinceDrop(highClass: Boolean): Int {
        return if (highClass) getConfig().highClassRollsSinceDrop
        else getConfig().diceRollsSinceDrop
    }

    private fun getRolls(highClass: Boolean, both: Boolean): Int {
        if (both) return getConfig().diceRolls + getConfig().highClassRolls
        else if (highClass) return getConfig().highClassRolls
        else return getConfig().diceRolls
    }

    private fun getCost(highClass: Boolean, both: Boolean): Long {
        if (both) return getConfig().diceRolls * DICE_COST + getConfig().highClassRolls * HIGH_CLASS_COST
        else if (highClass) return getConfig().highClassRolls * HIGH_CLASS_COST
        else return getConfig().diceRolls * DICE_COST
    }

    private fun getProfit(highClass: Boolean, both: Boolean): Long {
        if (both) return getConfig().diceWins * DICE_PROFIT + getConfig().highClassWins * HIGH_CLASS_PROFIT
        else if (highClass) return getConfig().highClassWins * HIGH_CLASS_PROFIT
        else return getConfig().diceWins * DICE_PROFIT
    }

    private fun getDyes(highClass: Boolean, both: Boolean): Int {
        if (both) return getConfig().diceDyes + getConfig().highClassDyes
        else if (highClass) return getConfig().highClassDyes
        else return getConfig().diceDyes
    }

    private fun getWins(highClass: Boolean, both: Boolean): Int {
        if (both) return getConfig().diceWins + getConfig().highClassWins
        else if (highClass) return getConfig().highClassWins
        else return getConfig().diceWins
    }

    private fun getDryStreak(highClass: Boolean, both: Boolean): Int {
        if (both) return getConfig().highestDiceRollSinceDrop.coerceAtLeast(getConfig().highestHighClassRollSinceDrop)
        else if (highClass) return getConfig().highestHighClassRollSinceDrop
        else return getConfig().highestDiceRollSinceDrop
    }

    private fun getQuickest(highClass: Boolean, both: Boolean): Int {
        if (both) return getConfig().lowestDiceRollSinceDrop.coerceAtLeast(getConfig().lowestHighClassRollSinceDrop)
        else if (highClass) return getConfig().lowestHighClassRollSinceDrop
        else return getConfig().lowestDiceRollSinceDrop
    }

    private fun printStats(highClass: Boolean, both: Boolean = false) {
        val type = if (both) "Dice" else if (highClass) HIGH_CLASS else DICE
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("$type Tracker", ChatFormatting.RED)
            }
        )
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("You have rolled ${getRolls(highClass, both)} times", ChatFormatting.YELLOW)
            }
        )
        Utils.sendMessageToPlayer("  ")

        val cost = getCost(highClass, both)
        val profit = getProfit(highClass, both)
        if (profit - cost > 0) {
            Utils.sendMessageToPlayer(
                Utils.componentBuilder {
                    append("You have profited ") {
                        withColor(ChatFormatting.YELLOW)
                    }
                    append("${(profit - cost).addSeparators()} coins") {
                        withColor(ChatFormatting.GOLD)
                    }
                }
            )
        } else {
            Utils.sendMessageToPlayer(
                Utils.componentBuilder {
                    append("You have lost ") {
                        withColor(ChatFormatting.YELLOW)
                    }
                    append("${(profit - cost).addSeparators()} coins") {
                        withColor(ChatFormatting.RED)
                    }
                }
            )
        }
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                append("After spending ")
                append("${cost.addSeparators()} coins") {
                    withColor(ChatFormatting.GOLD)
                }
                append(" on rolling")
                withColor(ChatFormatting.YELLOW)
            }
        )
        Utils.sendMessageToPlayer("    ")
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("You have dropped ", ChatFormatting.YELLOW)
                append("${getDyes(highClass, both)} Archfiend Dyes") {
                    withColor(ChatFormatting.RED)
                }
            }
        )
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("And rolled ${getWins(highClass, both)}", ChatFormatting.YELLOW)
                appendWithColor(" 6", ChatFormatting.DARK_PURPLE)
            }
        )
        Utils.sendMessageToPlayer("         ")
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("Your biggest dry streak is ${getDryStreak(highClass, both)}", ChatFormatting.YELLOW)
            }
        )
        Utils.sendMessageToPlayer(
            Utils.componentBuilder {
                appendWithColor("And your quickest win is ${getQuickest(highClass, both)}", ChatFormatting.YELLOW)
            }
        )
    }

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        val string = message.string
        if (!string.matches(regex)) return

        val diceType = regex.group(string, "type")
        val highClass = when (diceType) {
            HIGH_CLASS -> true
            DICE -> false
            else -> {
                Utils.debug("Unknown dice type $diceType")
                return
            }
        }

        val roll = regex.group(string, "number") ?: "1"
        addRoll(highClass)
        if (roll == "7") {
            addDye(highClass)
        } else if (roll == "6") {
            addWin(highClass)
        }
        DelayedRuns.schedule(5) {
            ConfigManager.save()
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "dice" {
                runs {
                    printStats(true, true)
                }
                "archfiend" {
                    runs {
                        printStats(false, false)
                    }
                }
                "highClass" {
                    runs {
                        printStats(true, false)
                    }
                }
                "advanced" {
                    runs {
                        val config = getConfig()
                        Utils.sendMessageToPlayer("$DICE Dice:")
                        Utils.sendMessageToPlayer("Rolls ${config.diceRolls}")
                        Utils.sendMessageToPlayer("6's ${config.diceWins}")
                        Utils.sendMessageToPlayer("Dyes ${config.diceDyes}")
                        Utils.sendMessageToPlayer("Rolls Since Last Drop ${config.diceRollsSinceDrop}")
                        Utils.sendMessageToPlayer("Biggest Dry Streak ${config.highestDiceRollSinceDrop}")
                        Utils.sendMessageToPlayer("Quickest Win ${config.lowestDiceRollSinceDrop}")

                        Utils.sendMessageToPlayer("")

                        Utils.sendMessageToPlayer("$HIGH_CLASS Dice:")
                        Utils.sendMessageToPlayer("Rolls ${config.highClassRolls}")
                        Utils.sendMessageToPlayer("6's ${config.highClassWins}")
                        Utils.sendMessageToPlayer("Dyes ${config.highClassDyes}")
                        Utils.sendMessageToPlayer("Rolls Since Last Drop ${config.highClassRollsSinceDrop}")
                        Utils.sendMessageToPlayer("Biggest Dry Streak ${config.highestHighClassRollSinceDrop}")
                        Utils.sendMessageToPlayer("Quickest Win ${config.lowestHighClassRollSinceDrop}")

                    }
                }
            }
        }
    }
}

class DiceConfig : ModuleConfig() {
    @Expose
    var diceRolls: Int = 0
    // Wins only count if you get 6, not 7
    @Expose
    var diceWins: Int = 0
    @Expose
    var diceDyes: Int = 0
    @Expose
    var diceRollsSinceDrop: Int = 0
    @Expose
    var highestDiceRollSinceDrop: Int = 0
    @Expose
    var lowestDiceRollSinceDrop: Int = 0


    @Expose
    var highClassRolls: Int = 0
    @Expose
    var highClassWins: Int = 0
    @Expose
    var highClassDyes: Int = 0
    @Expose
    var highClassRollsSinceDrop: Int = 0
    @Expose
    var highestHighClassRollSinceDrop: Int = 0
    @Expose
    var lowestHighClassRollSinceDrop: Int = 0

}