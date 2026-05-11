package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.commands.TaskConfig
import com.nopo.features.EquipmentDisplayConfig
import com.nopo.features.PetConfig
import com.nopo.features.garden.AshwreathConfig
import com.nopo.features.slayer.BossesSinceDropConfig

class Config {

    @Expose
    var firstTime = true

    @Expose
    var dev: Boolean? = null

    @Expose
    var useLocalJson: Boolean? = null

    @Expose
    var wokeConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    @Expose
    var overflowPetLevel = ModuleConfig()

    @Expose
    var ashwreath = AshwreathConfig(false)

    @Expose
    var rareCrop = ModuleConfig()

    @Expose
    var tasks = TaskConfig()

    @Expose
    var bossesSinceDrop = BossesSinceDropConfig()

    @Expose
    var partyFinderKickButton = ModuleConfig()

    @Expose
    var equipmentDisplay = EquipmentDisplayConfig()

    @Expose
    var petDisplay = PetConfig()

    @Expose
    var powderCoatingHider = ModuleConfig()

    @Expose
    var skyhanniTrackerTotem = ModuleConfig(false)

    @Expose
    var debug = ModuleConfig(false)

}