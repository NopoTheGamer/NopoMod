package com.nopo.utils

import com.nopo.NopoMod
import com.nopo.events.IslandChange
import com.nopo.events.ScoreboardChange
import com.nopo.events.TickEvent
import com.nopo.module.Module
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.Scoreboard
import kotlin.jvm.optionals.getOrNull


object HypixelUtils : Module("hypixel utils", needsToggle = false), TickEvent {

    var map: String? = null
    var mode: String? = null
    var currentIsland: IslandType = IslandType.UNKNOWN

    init {
        HypixelModAPI.getInstance().createHandler<ClientboundLocationPacket?>(
            ClientboundLocationPacket::class.java
        ) { packet: ClientboundLocationPacket ->
            map = packet.map.getOrNull()
            mode = packet.mode.getOrNull()
            val prev = currentIsland
            currentIsland = IslandType.UNKNOWN
            for (island in IslandType.entries) {
                if (island.map == map && island.mode == mode) {
                    currentIsland = island
                    break
                }
            }
            NopoMod.modules.forEach {
                if (it is IslandChange) {
                    it.onWorldSwap(currentIsland, prev)
                }
            }
        }

        Utils.registerDebugScreenEntry("current_area", { true }) {
            add("[Nopo] Current Map: $map")
            add("[Nopo] Current Mode: $mode")
            add("[Nopo] Current Island: $currentIsland")
        }
    }

    private fun getScoreboard(): List<Component> {
        val scoreboard = Minecraft.getInstance().level?.scoreboard ?: return emptyList()
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()
        val scores = scoreboard.listPlayerScores(objective)
        return getScoreboardNames(scores, scoreboard).reversed()
    }

    private fun getScoreboardNames(scores: Collection<PlayerScoreEntry>, scoreboard: Scoreboard): List<Component> {
        return scores.sortedBy { it.value }.map {
            val team = scoreboard.getPlayersTeam(it.owner)
            Component.empty().also { main ->
                team?.playerPrefix?.apply {
                    if (siblings.isNotEmpty()) siblings.forEach { sibling -> main.append(sibling) }
                    else main.append(this)
                }
                team?.playerSuffix?.apply {
                    if (siblings.isNotEmpty()) siblings.forEach { sibling -> main.append(sibling) }
                    else main.append(this)
                }
            }
        }
    }

    private var scoreboardLines: List<Component> = emptyList()

    override fun onTick(totalTicks: Int) {
        val new = getScoreboard()

        if (new != scoreboardLines) {
            val old = scoreboardLines
            scoreboardLines = new
            for (module in NopoMod.modules) {
                if (module is ScoreboardChange) {
                    val added = new - old.toSet()
                    val removed = old - new.toSet()
                    module.onScoreboardChange(added, removed, scoreboardLines, old)
                }
            }
        }
    }
}