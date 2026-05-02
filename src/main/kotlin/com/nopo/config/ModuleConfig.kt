package com.nopo.config

import com.google.gson.annotations.Expose

open class ModuleConfig(default: Boolean = true) {

   @Expose
   var enabled: Boolean = default
}