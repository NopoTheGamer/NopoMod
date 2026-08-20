package com.nopo.features.pets

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nopo.NopoMod
import com.nopo.events.TooltipEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Rarity
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object OverflowPetLevels : FeatureModule("overflowPetLevels", NopoMod.config.overflowPetLevel,
    ConfigData(
        Component.literal("Overflow Pet Levels"),
        componentBuilder {
            append("Shows overflow levels in the pets lore")
        }
    )), TooltipEvent {

    override fun onTooltip(itemStack: ItemStack, lore: MutableList<Component>) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (itemStack.item != Items.PLAYER_HEAD) return
        val nbt = itemStack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return
        if (!nbt.contains("petInfo")) return
        val petinfo = nbt.get("petInfo")?.asString()?.orElse(null) ?: return
        val json: JsonObject = Gson().fromJson(petinfo, JsonObject::class.java)
        if (json.has("exp")) {
            val xp = json.get("exp")?.asFloat ?: return
            for ((index, text) in lore.withIndex()) {
                if (text.string.contains("MAX LEVEL")) {
                    val newComponent = componentBuilder {
                        append("MAX LEVEL") {
                            withColor(ChatFormatting.AQUA)
                            withStyle(ChatFormatting.BOLD)
                        }
                        append(" [")
                        appendWithColor("${calcLevel(xp)}✦", ChatFormatting.GOLD)
                        append("]")
                        withColor(ChatFormatting.GRAY)
                    }
                    lore[index] = newComponent
                    return
                }
            }
        }
    }

    fun getCalculativeXpForLevel(level: Int, rarity: Rarity = Rarity.LEGENDARY): Int {
        var xp = 0
        for (i in 0 until level) {
            xp += getXpForLevel(i, rarity)
        }
        return xp
    }

    fun getXpForLevel(level: Int, rarity: Rarity = Rarity.LEGENDARY): Int {
        val offset = getOffset(rarity) + level
        return if (listOfXp.size > offset) {
            listOfXp[offset]
        } else {
            1886700
        }
    }

    fun calcLevel(xp: Float, rarity: Rarity = Rarity.LEGENDARY): Int {
        var exp = xp
        var i = 0
        while (exp > 0) {
            val xp = getXpForLevel(i, rarity)
            exp -= xp
            i++
        }
        if (i < 1) i = 1
        return i
    }

    fun calcLeftOverXp(xp: Float, rarity: Rarity = Rarity.LEGENDARY): Float {
        var exp = xp
        var i = 0
        while (exp > 0) {
            val xp = getXpForLevel(i, rarity)
            if (exp > xp) exp -= xp
            else return exp
            i++
        }
        return -1f
    }

    fun getOffset(rarity: Rarity): Int {
        return when (rarity) {
            Rarity.COMMON -> 0
            Rarity.UNCOMMON -> 6
            Rarity.RARE -> 11
            Rarity.EPIC -> 15
            else -> 20
        }
    }

    private val listOfXp = listOf(
        100,
        110,
        120,
        130,
        145,
        160,
        175,
        190,
        210,
        230,
        250,
        275,
        300,
        330,
        360,
        400,
        440,
        490,
        540,
        600,
        660,
        730,
        800,
        880,
        960,
        1050,
        1150,
        1260,
        1380,
        1510,
        1650,
        1800,
        1960,
        2130,
        2310,
        2500,
        2700,
        2920,
        3160,
        3420,
        3700,
        4000,
        4350,
        4750,
        5200,
        5700,
        6300,
        7000,
        7800,
        8700,
        9700,
        10800,
        12000,
        13300,
        14700,
        16200,
        17800,
        19500,
        21300,
        23200,
        25200,
        27400,
        29800,
        32400,
        35200,
        38200,
        41400,
        44800,
        48400,
        52200,
        56200,
        60400,
        64800,
        69400,
        74200,
        79200,
        84700,
        90700,
        97200,
        104200,
        111700,
        119700,
        128200,
        137200,
        146700,
        156700,
        167700,
        179700,
        192700,
        206700,
        221700,
        237700,
        254700,
        272700,
        291700,
        311700,
        333700,
        357700,
        383700,
        411700,
        441700,
        476700,
        516700,
        561700,
        611700,
        666700,
        726700,
        791700,
        861700,
        936700,
        1016700,
        1101700,
        1191700,
        1286700,
        1386700,
        1496700,
        1616700,
        1746700,
        1886700,
    )
}