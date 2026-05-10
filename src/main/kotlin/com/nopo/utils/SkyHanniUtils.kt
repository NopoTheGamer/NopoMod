package com.nopo.utils

import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.ItemStack

object SkyHanniUtils {

    val isSkyHanniLoaded = FabricLoader.getInstance().isModLoaded("skyhanni")

    fun getRepoStack(id: String): ItemStack? {
        if (!isSkyHanniLoaded) return null
        return id.toInternalName().getItemStack()
    }
}