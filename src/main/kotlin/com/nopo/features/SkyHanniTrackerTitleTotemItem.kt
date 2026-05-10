package com.nopo.features

import com.nopo.NopoMod
import com.nopo.module.FeatureModule
import com.nopo.utils.SkyHanniUtils

object SkyHanniTrackerTitleTotemItem : FeatureModule("skyhanniTrackerItemPopup", NopoMod.config.skyhanniTrackerTotem, { !SkyHanniUtils.isSkyHanniLoaded})