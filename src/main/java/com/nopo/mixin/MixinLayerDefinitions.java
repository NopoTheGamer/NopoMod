package com.nopo.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@Mixin(LayerDefinitions.class)
public class MixinLayerDefinitions {

    @Shadow
    @Final
    private static CubeDeformation INNER_ARMOR_DEFORMATION;

    @Shadow
    @Final
    private static CubeDeformation OUTER_ARMOR_DEFORMATION;

    @Inject(method = "createRoots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ArmorModelSet;putFrom(Lnet/minecraft/client/renderer/entity/ArmorModelSet;Lcom/google/common/collect/ImmutableMap$Builder;)V", ordinal = 0))
    private static void addBabyPlayer(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir, @Local ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        ArmorModelSet<LayerDefinition> map = PlayerModel.createArmorMeshSet(INNER_ARMOR_DEFORMATION, OUTER_ARMOR_DEFORMATION).map(meshDefinition -> LayerDefinition.create(meshDefinition, 64, 32)).map(a -> a.apply(HumanoidModel.BABY_TRANSFORMER));
        Objects.requireNonNull(SmallPlayers.getPLAYER_BABY_ARMOR()).putFrom(map, builder);
        Objects.requireNonNull(SmallPlayers.getPLAYER_BABY_SLIM_ARMOR()).putFrom(map, builder);
        builder.put(Objects.requireNonNull(SmallPlayers.getPLAYER_BABY()), LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).apply(HumanoidModel.BABY_TRANSFORMER));
        builder.put(Objects.requireNonNull(SmallPlayers.getPLAYER_BABY_SLIM()), LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64).apply(HumanoidModel.BABY_TRANSFORMER));
    }
}
