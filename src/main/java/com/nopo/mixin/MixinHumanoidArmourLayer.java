package com.nopo.mixin;

//@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmourLayer {

    /*@ModifyReturnValue(method = "getArmorModel", at = @At("RETURN"))
    private HumanoidModel<?> getArmourModel(HumanoidModel<?> original, HumanoidRenderState state, EquipmentSlot slot) {
        if (state instanceof AvatarRenderState avatarRenderState && original instanceof PlayerModel && avatarRenderState.getDataOrDefault(SmallPlayers.getKey(), false)) {
            // return the baby model thingy not sure yet
        }
    }*/
}
