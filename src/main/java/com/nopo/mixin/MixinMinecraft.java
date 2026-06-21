package com.nopo.mixin;

import com.nopo.NopoMod;
import com.nopo.features.FastAOTVConfig;
import com.nopo.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z", shift = At.Shift.BEFORE))
    public void delay(CallbackInfo ci) {
        FastAOTVConfig fastAotvConfig = NopoMod.config.getFastAotvConfig();
        if (!fastAotvConfig.getEnabled()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (Utils.getItemId(stack).equals("ASPECT_OF_THE_VOID")) this.rightClickDelay = fastAotvConfig.getTickDelayAmount();
    }
}
