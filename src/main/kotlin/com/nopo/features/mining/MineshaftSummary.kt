package com.nopo.features.mining

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.IslandChange
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.ItemManager
import com.nopo.utils.Rarity.Companion.getColourStyle
import com.nopo.utils.TabWidget
import com.nopo.utils.Utils
import com.nopo.utils.Utils.addSeparators
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.format
import com.nopo.utils.Utils.formatInt
import com.nopo.utils.Utils.hover
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import kotlin.time.Duration.Companion.milliseconds

object MineshaftSummary : FeatureModule("mineshaftSummary", NopoMod.config.mineshaftSummaryConfig,
    ConfigData(
        Component.literal("Mineshaft Summary"),
        componentBuilder {
            append("Sends a message with your total profit when you leave a mineshaft\nNPC prices only")
        }
    )
    ), ChatEvent, IslandChange, CommandRegistration, TickEvent {

    private val sackItemRegex = Regex("\\s+(?<amount>[+-]?[\\d,]+)\\s+(?<item>[a-zA-Z0-9 ]+) \\([a-zA-Z0-9 ]+ Sack\\)")
    private val corpseLootRegex = Regex("(?<item>[a-zA-Z0-9 ]+?)(?<amount> x[\\d,]+?)?$")
    private val cleanRegex = Regex("[^a-zA-Z0-9\n()\\-:▬' ]")
    private val powderRegex = Regex(" Glacite: (?<amount>[\\d,]+)")

    private var gainedItems = mutableMapOf<String, Int>()
    private var startingPowder = -1
    private var currentPowder = -1
    private var entryTime = 0L

    private var debug = false

    private fun inMineshaft() = HypixelUtils.currentIsland == IslandType.MINESHAFT || debug

    override fun onChat(message: Component, actionBar: Boolean) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (!inMineshaft()) return
        if (actionBar) return
        val string = message.string
        corpse(cleanRegex.replace(string, "").trim())
        if (!string.startsWith("[Sacks] ")) return
        val addedItems = getItemsFromMessage(message, true)
        val removedItems = getItemsFromMessage(message, false)
        gainedItems = combine(gainedItems, combine(addedItems, removedItems))
    }

    override fun onTick(totalTicks: Int) {
        if (!inMineshaft()) return
        for (line in TabWidget.POWDERS.lines) {
            if (powderRegex.matches(line.string)) {
                val match = powderRegex.matchEntire(line.string) ?: return
                val powder = match.groups["amount"]?.value?.formatInt() ?: return
                if (startingPowder == -1) startingPowder = powder
                currentPowder = powder
            }
        }
    }

    var inCorpse = false
    fun corpse(message: String) {
        if (message == "REWARDS") {
            inCorpse = true
            return
        }
        if (message == "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬") {
            inCorpse = false
            return
        }
        if (inCorpse && corpseLootRegex.matches(message)) {
            val match = corpseLootRegex.matchEntire(message) ?: return
            val item = match.groups["item"]?.value ?: return
            if (NopoMod.data?.corpseDrops?.firstOrNull { it.name == item } != null) {
                val amount = match.groups["amount"]?.value?.replace(" x", "")?.formatInt() ?: 1
                gainedItems = combine(gainedItems, mapOf(item to amount))
            }
        }
    }

    fun getItemsFromMessage(message: Component, added: Boolean): Map<String, Int> {
        for (component in message.siblings) {
            component.hover?.let {
                val map = mutableMapOf<String, Int>()
                val string = it.string
                val startsWith = if (added) "Added items:" else "Removed items:"
                if (string.startsWith(startsWith)) {
                    val items = cleanSackMessage(string).split("\n")
                    for (item in items) {
                        if (sackItemRegex.matches(item)) {
                            val match = sackItemRegex.matchEntire(item) ?: continue
                            val amount = match.groups["amount"]?.value?.formatInt() ?: continue
                            val item = match.groups["item"]?.value ?: continue
                            map[item] = amount
                        }
                    }
                    return map
                }
            }
        }
        return emptyMap()
    }

    fun cleanSackMessage(string: String): String {
        return cleanRegex.replace(string, "").trim()
            .replace("Added items:\n", "")
            .replace("Removed items:\n", "")
            .replace("\n\nThis message can be disabled in the settings.", "")
    }

    override fun onWorldSwap(newIsland: IslandType, oldIsland: IslandType) {
        if (config.enabled && !gainedItems.isEmpty()) printSummary(gainedItems)
        gainedItems.clear()
        inCorpse = false
        currentPowder = -1
        startingPowder = -1
        entryTime = -1
        if (newIsland == IslandType.MINESHAFT) entryTime = System.currentTimeMillis()
    }

    fun printSummary(items: MutableMap<String, Int>) {
        val finalMessage = Component.empty()
        val hoverText = Component.empty()
        val summaryMessage = componentBuilder {
            appendWithColor("Mineshaft Summary", ChatFormatting.GREEN)
        }
        finalMessage.append(summaryMessage)
        hoverText.append(summaryMessage)
        val rngSet = mutableSetOf<String>()
        val sortedMap = items.toList().sortedBy { (name, amount) ->
            val corpseDrop = NopoMod.data?.corpseDrops?.firstOrNull { it.name == name }
            val item = ItemManager.getByName(name)
            if (corpseDrop != null && corpseDrop.rng != false) {
                rngSet.add(name)
                -100_000_000
            } else if (item != null) {
                -(amount * (item.npcSellPrice ?: 1.0)).toInt()
            } else -amount
        }.toMap()
        var profit = 0
        var amountToDisplay = 5 + rngSet.size
        for ((name, amount) in sortedMap) {
            val item = ItemManager.getByName(name)
            val price = (amount * (item?.npcSellPrice ?: 0.0)).toInt()
            profit += price
            amountToDisplay--
            val message = Component.empty()
            if (item != null) {
                message.append(
                    componentBuilder {
                        append("\n")
                        appendWithColor(item.name ?: name.trim(), item.rarity.getColourStyle())
                        append(" ")
                        appendWithColor("x${amount.addSeparators()}", ChatFormatting.DARK_GRAY)
                        if (price != 0) {
                            append(" ")
                            appendWithColor("(", ChatFormatting.GRAY)
                            appendWithColor("${price.addSeparators()} Coins", ChatFormatting.GOLD)
                            appendWithColor(")", ChatFormatting.GRAY)
                        }
                    }
                )
            } else {
                message.append(
                    componentBuilder {
                        append("\n")
                        appendWithColor(name, ChatFormatting.YELLOW)
                        append(" ")
                        appendWithColor(" x$amount", ChatFormatting.DARK_GRAY)
                    }
                )
            }
            if (amountToDisplay >= 0) finalMessage.append(message)
            hoverText.append(message)
        }

        val powderMessage = componentBuilder {
            appendWithColor("\nGlacite Powder: ", ChatFormatting.AQUA)
            appendWithColor((currentPowder - startingPowder).addSeparators(), ChatFormatting.DARK_GRAY)
        }
        if (startingPowder != -1 && currentPowder - startingPowder > 0) {
            finalMessage.append(powderMessage)
            hoverText.append(powderMessage)
        }

        val timeMessage = componentBuilder {
            appendWithColor("\nTime in Mineshaft: ", ChatFormatting.YELLOW)
            appendWithColor((System.currentTimeMillis() - entryTime).milliseconds.format(), ChatFormatting.DARK_GRAY)
        }
        finalMessage.append(timeMessage)
        hoverText.append(timeMessage)

        val profitMessage = componentBuilder {
            appendWithColor("\nTotal Profit: ", ChatFormatting.GREEN)
            appendWithColor("${profit.addSeparators()} Coins", ChatFormatting.GOLD)
        }
        finalMessage.append(profitMessage)
        hoverText.append(profitMessage)

        DelayedRuns.schedule(20) {
            Utils.sendMessageToPlayer(
                componentBuilder {
                    append(finalMessage) {
                        hover = hoverText
                    }
                }
            )
        }
    }

    fun combine(first: Map<String, Int>, second: Map<String, Int>): MutableMap<String, Int> {
        val result = mutableMapOf<String, Int>()

        first.forEach { (key, i) ->
            if (second.contains(key)) result[key] = i + second[key]!!
            else result[key] = i
        }

        second.forEach { (key, i) ->
            if (!result.contains(key)) result[key] = i
        }

        return result
    }

    override fun createCommand(): Commodore? {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "print" {
                        runs {
                            printSummary(gainedItems)
                        }
                    }
                    "reset" {
                        runs {
                            gainedItems.clear()
                        }
                    }
                    "debug" {
                        runs {
                            debug = !debug
                        }
                    }
                }
            }
        }
    }

}

data class CorpseDrops(
    @Expose val name: String?,
    @Expose val rng: Boolean?,
)