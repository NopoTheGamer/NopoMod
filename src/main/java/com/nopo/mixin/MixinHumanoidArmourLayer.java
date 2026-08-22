package com.nopo.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmourLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {
    @Definition(id = "entityType", field = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;entityType:Lnet/minecraft/world/entity/EntityType;")
    @Definition(id = "state", local = @Local(type = HumanoidRenderState.class, name = "state", argsOnly = true))
    @Definition(id = "ARMOR_STAND", field = "Lnet/minecraft/world/entity/EntityType;ARMOR_STAND:Lnet/minecraft/world/entity/EntityType;")
    @Expression("state.entityType != ARMOR_STAND")
    @WrapOperation(method = "renderArmorPiece", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean modifyArmorType(Object left, Object right, Operation<Boolean> original, @Local(argsOnly = true, name = "state") S state) {
        boolean small = state.getDataOrDefault(SmallPlayers.getKey(), false);
        if (small) {
            return false;
        }
        return original.call(left, right);
    }
}
