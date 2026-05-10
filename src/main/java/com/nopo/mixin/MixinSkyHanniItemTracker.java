package com.nopo.mixin;

import at.hannibal2.skyhanni.config.features.misc.tracker.ItemTrackerGenericConfig;
import com.llamalad7.mixinextras.sugar.Local;
import com.nopo.NopoMod;
import com.nopo.utils.SkyHanniUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker")
public abstract class MixinSkyHanniItemTracker {

    @Shadow
    protected abstract ItemTrackerGenericConfig.ItemTrackerConfig getItemTrackerConfig();

    // this is the worlds worst mixin i think
    // i could not get it to accept the real method name so i had to do * and narrow it down using params sob emoji
    @Inject(method = "*", at = @At(value = "TAIL"))
    public void showAnimationForDrop(String internalName, int amount, boolean message, CallbackInfo ci, @Local(ordinal = 0) double price) {
        if (!NopoMod.config.getSkyhanniTrackerTotem().getEnabled()) return;
        if (getItemTrackerConfig().getWarnings().getTitle() && price >= getItemTrackerConfig().getWarnings().getMinimumTitle()) {
            ItemStack stack = SkyHanniUtils.INSTANCE.getRepoStack(internalName);
            if (stack == null) return;
            Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
        }
    }
}
