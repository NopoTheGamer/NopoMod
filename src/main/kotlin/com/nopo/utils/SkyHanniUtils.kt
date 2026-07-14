package com.nopo.utils

import at.hannibal2.skyhanni.data.SackApi.getAmountInSacksOrNull
import at.hannibal2.skyhanni.utils.BlockUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Vec3i
import net.minecraft.world.item.ItemStack

object SkyHanniUtils {

    val isSkyHanniLoaded = FabricLoader.getInstance().isModLoaded("skyhanni")

    fun getRepoStack(id: String): ItemStack? {
        if (!isSkyHanniLoaded) return null
        try {
            return id.toInternalName().getItemStack()
        } catch (_: Exception) {
            return null
        }
    }

    fun getAmountInSack(id: String): Int {
        if (!isSkyHanniLoaded) return 0
        try {
            return id.toInternalName().getAmountInSacksOrNull() ?: 0
        } catch (_: Exception) {
            return 0
        }
    }

    fun getTargetedBlock(): Vec3i? {
        if (!isSkyHanniLoaded) return null
        try {
            val pos = BlockUtils.getTargetedBlockAtDistance(300.0) ?: return null
            return Vec3i(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        } catch (_: Exception) {
            return null
        }
    }
}