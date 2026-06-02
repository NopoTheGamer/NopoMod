package com.nopo.utils

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.ItemStack

object SkyHanniUtils {

    val isSkyHanniLoaded = FabricLoader.getInstance().isModLoaded("skyhanni")

    fun getRepoStack(id: String): ItemStack? {
        if (!isSkyHanniLoaded) return null
        /*try {
            return id.toInternalName().getItemStack()
        } catch (_: Exception) {
            return null
        }*/
        return null
    }

    fun getAmountInSack(id: String): Int {
        if (!isSkyHanniLoaded) return 0
        /*try {
            return id.toInternalName().getAmountInSacksOrNull() ?: 0
        } catch (_: Exception) {
            return 0
        }*/
        return 0
    }
}