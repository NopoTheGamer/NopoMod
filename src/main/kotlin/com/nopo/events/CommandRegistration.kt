package com.nopo.events

import com.github.stivais.commodore.Commodore

interface CommandRegistration {

    fun createCommand(): Commodore
}