package com.nopo.mixin;

import com.nopo.events.FabricEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayerEntity {

    @Inject(method = "getDisplayName", at = @At(value = "RETURN"), cancellable = true)
    public void getName(CallbackInfoReturnable<Component> cir) {
        Player entity = (Player) (Object) this;
        Component newComponent = FabricEvents.postEntityEvent(entity, cir.getReturnValue());
        if (newComponent != null) cir.setReturnValue(newComponent);
    }

}
