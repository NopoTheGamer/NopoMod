package com.nopo.mixin;

import com.nopo.events.ParticleEvent;
import com.nopo.events.ParticleEventData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Inject(
            method = "handleParticleEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void postParticleEvent(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        ParticleEventData data = new ParticleEventData(
                packet.getParticle(),
                new Vec3(packet.getX(), packet.getY(), packet.getZ()),
                new Vec3(packet.getXDist(), packet.getYDist(), packet.getZDist()),
                packet.getCount(),
                packet.getMaxSpeed()
        );
        if (!ParticleEvent.Companion.postParticleEvent(data)) ci.cancel();
        
    }
}
