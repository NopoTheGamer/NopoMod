package com.nopo.config

import com.google.gson.annotations.Expose

class Config {

    @Expose
    var wokeConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    @Expose
    var debug = ModuleConfig(false)

    // dont try saving stuff here
    var dummyConfig = ModuleConfig()

}