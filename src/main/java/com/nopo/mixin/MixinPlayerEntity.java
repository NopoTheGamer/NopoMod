package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nopo.events.FabricEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class MixinPlayerEntity {

    @WrapMethod(method = "getDisplayName")
    public Component getName(Operation<Component> original) {
        Player entity = (Player) (Object) this;
        Component originalComponent = original.call();
        Component newComponent = FabricEvents.postEntityEvent(entity, originalComponent);
        if (newComponent != null) return newComponent;
        return originalComponent;
    }

}
