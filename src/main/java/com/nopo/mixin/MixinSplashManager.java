package com.nopo.mixin;

import com.nopo.utils.Utils;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;

@Mixin(SplashManager.class)
public class MixinSplashManager {
    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    public void prideMonth(CallbackInfoReturnable<SplashRenderer> cir) {
        if (MonthDay.from(ZonedDateTime.now()).getMonth() == Month.JUNE) {
            cir.setReturnValue(new SplashRenderer(Utils.prideMonthComponent()));
        }
    }
}
