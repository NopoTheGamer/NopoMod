package com.nopo.module

open class BaseModule(
    val moduleName: String,
    val shouldBeHidden: () -> Boolean = { false }
)