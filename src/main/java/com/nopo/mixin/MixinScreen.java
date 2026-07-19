package com.nopo.mixin;

import com.nopo.NopoMod;
import com.nopo.events.ScreenRendering;
import com.nopo.features.inventory.WardrobeKeybinds;
import com.nopo.module.BaseModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (WardrobeKeybinds.onKeyPress((Screen) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    public void onRender(GuiGraphicsExtractor guiGraphics, int x, int y, float delta, CallbackInfo ci) {
        for (BaseModule module : NopoMod.INSTANCE.getModules()) {
            if (module instanceof ScreenRendering event) {
                event.renderAfterScreen(guiGraphics);
            }
        }
    }
}
