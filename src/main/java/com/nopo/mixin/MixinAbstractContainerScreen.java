package com.nopo.mixin;

import com.nopo.features.inventory.HarpMisclick;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void preventClick(Slot slot, int slotId, int buttonNum, ClickType clickType, CallbackInfo ci) {
        if (HarpMisclick.onSlotClick(slot, slotId, buttonNum, clickType)) {
            ci.cancel();
        }
    }

}
