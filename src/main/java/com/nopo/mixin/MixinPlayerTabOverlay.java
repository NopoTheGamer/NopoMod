package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nopo.features.silly.CosmeticData;
import com.nopo.features.silly.Cosmetics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlay {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"))
    public Component replaceName(PlayerTabOverlay instance, PlayerInfo playerInfo, Operation<Component> original) {
        Component component = original.call(instance, playerInfo);
        Component tabListDisplayName = playerInfo.getTabListDisplayName();
        CosmeticData cosmeticData = Cosmetics.getCosmeticDataFromTab(tabListDisplayName);
        if (tabListDisplayName == null) cosmeticData = Cosmetics.getCosmeticData(playerInfo.getProfile().id());
        Component newComponent = Cosmetics.getNameFromCosmeticData(component, cosmeticData);
        if (newComponent != null) return newComponent;
        return component;
    }
}
