package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmourLayer {

    @ModifyReturnValue(method = "getArmorModel", at = @At("RETURN"))
    private HumanoidModel<?> getArmourModel(HumanoidModel<?> original, HumanoidRenderState state, EquipmentSlot slot) {
        if (state instanceof AvatarRenderState avatarRenderState && original instanceof PlayerModel && avatarRenderState.getDataOrDefault(SmallPlayers.getKey(), false)) {
            // return the baby model thingy not sure yet
        }
    }
}
