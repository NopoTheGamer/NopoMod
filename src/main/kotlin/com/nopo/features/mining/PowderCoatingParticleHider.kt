package com.nopo.features.mining

import com.nopo.NopoMod
import com.nopo.events.ParticleEvent
import com.nopo.events.ParticleEventData
import com.nopo.module.FeatureModule
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import org.joml.Vector3f

object PowderCoatingParticleHider : FeatureModule("hidePowderCoatingParticles", NopoMod.config.powderCoatingHider), ParticleEvent {

    val badColours = listOf(
        Vector3f(1f, 0.6f, 0f),
        Vector3f(1f, 1f, 1f),
    )

    override fun onParticleReceived(event: ParticleEventData): Boolean {
        if (event.options.type != ParticleTypes.DUST) return true
        if (event.options !is DustParticleOptions) return true
        val playerPos = Minecraft.getInstance().player?.position() ?: return true
        if (event.location.distanceToSqr(playerPos) > 2) return true

        val colour = event.options.color
        return colour !in badColours
    }
}