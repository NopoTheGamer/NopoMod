package com.nopo.features

import com.nopo.NopoMod
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.SkyHanniUtils
import net.minecraft.network.chat.Component

object SkyHanniTrackerTitleTotemItem : FeatureModule(
    "skyhanniTrackerItemPopup", NopoMod.config.skyhanniTrackerTotem, ConfigData(
        Component.literal("Totem Animation for SH Tracker Drops"),
        Component.literal("Plays the Totem Of Undying animation when you get a tracker drop that would show a title (5m coins or more by default)")
    ), { !SkyHanniUtils.isSkyHanniLoaded })