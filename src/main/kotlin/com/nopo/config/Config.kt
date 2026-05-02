package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.commands.TaskConfig
import com.nopo.features.AshwreathConfig
import com.nopo.features.slayer.BossesSinceDropConfig

class Config {

    @Expose
    var firstTime = true

    @Expose
    var dev: Boolean? = null

    @Expose
    var wokeConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    @Expose
    var overflowPetLevel = ModuleConfig()

    @Expose
    var ashwreath = AshwreathConfig()

    @Expose
    var tasks = TaskConfig()

    @Expose
    var bossesSinceDrop = BossesSinceDropConfig()

    @Expose
    var debug = ModuleConfig(false)

    // dont try saving stuff here
    var dummyConfig = ModuleConfig()

}