package com.nopo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Shadow
    protected M model;

    @Unique
    protected M modelCopy = null;

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    public void extract(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (livingEntityRenderState instanceof AvatarRenderState ars) {
            Object data = livingEntityRenderState.getData(SmallPlayers.getKey());
            if (data != null) {
                if (modelCopy == null) modelCopy = model;
                boolean isBaby = (boolean) data;
                boolean slim = ars.skin.model() == PlayerModelType.SLIM;
                if (isBaby) model = (M) (slim ? SmallPlayers.getPLAYER_MODEL_SLIM() : SmallPlayers.getPLAYER_MODEL());
            } else {
                if (modelCopy != null) {
                    model = modelCopy;
                }
            }
        }
    }
}
