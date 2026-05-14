package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.nopo.NopoMod;
import com.nopo.features.silly.Shoulder;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;", ordinal = 0))
    private static ModelPart makeBabyPlayerModel(EntityRendererProvider.Context instance, ModelLayerLocation modelLayerLocation, Operation<ModelPart> original, @Local(argsOnly = true) EntityRendererProvider.Context context, @Local(argsOnly = true) boolean bl) {
        SmallPlayers.setPLAYER_MODEL(new PlayerModel(context.bakeLayer(Objects.requireNonNull(SmallPlayers.getPLAYER_BABY())), bl));
        SmallPlayers.setPLAYER_MODEL_SLIM(new PlayerModel(context.bakeLayer(Objects.requireNonNull(SmallPlayers.getPLAYER_BABY_SLIM())), bl));
        return original.call(instance, modelLayerLocation);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z", ordinal = 0))
    private static boolean addBabyArmourLayer(AvatarRenderer instance, RenderLayer renderLayer, Operation<Boolean> original, @Local(argsOnly = true) EntityRendererProvider.Context context, @Local(argsOnly = true) boolean bl) {
        HumanoidArmorLayer humanoidArmorLayer = new HumanoidArmorLayer<>(
                instance,
                ArmorModelSet.bake(bl ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR, context.getModelSet(), modelPart -> new PlayerModel(modelPart, bl)),
                ArmorModelSet.bake(Objects.requireNonNull(bl ? SmallPlayers.getPLAYER_BABY_SLIM_ARMOR() : SmallPlayers.getPLAYER_BABY_ARMOR()), context.getModelSet(), modelPart -> new PlayerModel(modelPart, bl)),
                context.getEquipmentRenderer()
        );
        return original.call(instance, humanoidArmorLayer);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    public void extract(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;
        if (avatarRenderState.isInvisibleToPlayer) return;
        if (avatar instanceof AbstractClientPlayer entity) {
            GameProfile profile = entity.getGameProfile();
            if (profile.id().equals(NopoMod.INSTANCE.getData().getDevs().getFirst())) {
                avatarRenderState.parrotOnRightShoulder = Parrot.Variant.YELLOW_BLUE;
            }
            if (profile.id() == Minecraft.getInstance().player.getUUID()) {
                if (NopoMod.config.getSmallConfig().getEnabled()) {
                    avatarRenderState.isBaby = true;
                    avatarRenderState.ageScale = 0.5f;
                    avatarRenderState.setData(SmallPlayers.getKey(), true);
                }
                Map<@NotNull Shoulder, Parrot.@Nullable Variant> parrotMap = NopoMod.config.getParrotConfig().getParrots();
                if (parrotMap == null) return;
                if (parrotMap.get(Shoulder.LEFT) != null) {
                    avatarRenderState.parrotOnLeftShoulder = parrotMap.get(Shoulder.LEFT);
                }
                if (parrotMap.get(Shoulder.RIGHT) != null) {
                    avatarRenderState.parrotOnRightShoulder = parrotMap.get(Shoulder.RIGHT);
                }
            } else {
                if (NopoMod.config.getSmallConfig().getEveryone()) {
                    avatarRenderState.isBaby = true;
                    avatarRenderState.ageScale = 0.5f;
                    avatarRenderState.setData(SmallPlayers.getKey(), true);
                }
            }
        }
    }
}
