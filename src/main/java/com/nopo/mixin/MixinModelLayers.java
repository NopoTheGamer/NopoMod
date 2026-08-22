package com.nopo.mixin;

import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLayers.class)
public class MixinModelLayers {
    @Shadow
    private static ArmorModelSet<ModelLayerLocation> registerArmorSet(String string) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static ModelLayerLocation register(String string) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticInitializer(CallbackInfo ci) {
        SmallPlayers.setPLAYER_BABY(register("player_baby"));
        SmallPlayers.setPLAYER_BABY_ARMOR(registerArmorSet("player_baby"));
        SmallPlayers.setPLAYER_BABY_SLIM(register("player_slim_baby"));
        SmallPlayers.setPLAYER_BABY_SLIM_ARMOR(registerArmorSet("player_slim_baby"));
    }
}
