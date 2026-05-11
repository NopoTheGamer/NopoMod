package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.features.garden.RareCropConfig

class RareCropConfigHolder {
    @Expose
    val cropConfig = RareCropConfig()
}