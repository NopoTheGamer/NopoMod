package com.nopo.mixin;

import com.nopo.mixininterfaces.FunnyEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public class MixinSheep implements FunnyEntityData {

    @Unique
    boolean nopo$ravageSheep = false;

    @Override
    public boolean nopo$isFunny() {
        return nopo$ravageSheep;
    }

    @Override
    public void nopo$setFunny(boolean funny) {
        nopo$ravageSheep = funny;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType entityType, Level level, CallbackInfo ci) {
        nopo$setFunny(level.getRandom().nextInt(50) == 0);
    }
}
