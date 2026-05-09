package com.nopo.events

import com.nopo.NopoMod
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

interface ParticleEvent {
    fun onParticleReceived(event: ParticleEventData): Boolean

    companion object {
        fun postParticleEvent(event: ParticleEventData): Boolean {
            var allowed = true
            for (module in NopoMod.modules) {
                if (module is ParticleEvent) {
                    if (!module.onParticleReceived(event)) allowed = false
                }
            }
            return allowed
        }
    }
}

data class ParticleEventData(
    val options: ParticleOptions,
    val location: Vec3,
    val locationDist: Vec3,
    val count: Int,
    val speed: Float
)