package com.nopo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class MixinCapeLayer {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    public void noBaby(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g, CallbackInfo ci) {
        if (avatarRenderState.getDataOrDefault(SmallPlayers.getKey(), false)) ci.cancel();
    }
}
