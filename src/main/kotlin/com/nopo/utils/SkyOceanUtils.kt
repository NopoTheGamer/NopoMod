package com.nopo.utils

import com.nopo.utils.Utils.cleanColor
import me.owdding.skyocean.features.item.search.matcher.ItemMatcher
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.item.sources.system.TrackedItem
import me.owdding.skyocean.features.item.sources.system.TrackedItemBundle
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object SkyOceanUtils {

    val isSkyOceanLoaded = FabricLoader.getInstance().isModLoaded("skyocean")

    private val cleanNameRegex = Regex("(§.)|[^a-zA-Z0-9 ]")

    fun getItemCount(item: String): Pair<Component, Int>? {
        if (!isSkyOceanLoaded) return null
        try {
            val items = mutableListOf<TrackedItem>()
            // code airlifted from skyocean
            // why make it simple :)
            items.addAll(
                ItemSources.getAllItems().fold(mutableListOf()) { list, item ->
                    val (itemStack) = item

                    list.find { ItemMatcher.compare(it.itemStack, itemStack) }?.let {
                        if (it !is TrackedItemBundle) {
                            list.remove(it)
                            list.add(it.add(item))
                        } else {
                            it.add(item)
                        }
                        return@fold list
                    }

                    list.add(item)
                    list
                },
            )
            val item = items.firstOrNull {
                val name = it.itemStack.hoverName.string.cleanColor()
                val replace = cleanNameRegex.replace(name, "").trim()
                replace.equals(item, ignoreCase = true)
            } ?: return null
            return item.itemStack.hoverName to item.itemStack.count
        } catch (_: Exception) {
            return null
        }
    }
}