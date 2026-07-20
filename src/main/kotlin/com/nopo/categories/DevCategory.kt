package com.nopo.categories

import com.nopo.NopoMod
import com.nopo.events.TickEvent
import com.nopo.module.BaseModule
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import net.minecraft.network.chat.Component

object DevCategory : BaseModule("Dev"), Category

object DevUnlock : FeatureModule("fakeUnlockDev", NopoMod.config.fakeDevUnlock, ConfigData(
    Component.literal("Unlock Dev Mode?"),
    Component.literal("Probably only do this if you have a good reason")
))

object RealDevUnlock : FeatureModule("realUnlockDev", NopoMod.config.realDevUnlock, ConfigData(
    Component.literal("Are you sure...?"),
    Component.literal("You are up to no good I can tell")
), shouldBeHidden = { !NopoMod.config.fakeDevUnlock.enabled || NopoMod.config.realDevUnlock.enabled }, stillRegisterCommand = true), TickEvent {
    override fun onTick(totalTicks: Int) {
        if (!NopoMod.config.fakeDevUnlock.enabled) NopoMod.config.realDevUnlock.enabled = false
    }

}