package com.nopo.mixin;

import com.mojang.authlib.GameProfile;
import com.nopo.NopoMod;
import com.nopo.features.silly.Shoulder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    public void a(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;
        if (avatarRenderState.isInvisibleToPlayer) return;
        if (avatar instanceof AbstractClientPlayer entity) {
            GameProfile profile = entity.getGameProfile();
            if (profile.id() == NopoMod.INSTANCE.getData().getDevs().getFirst()) {
                avatarRenderState.parrotOnRightShoulder = Parrot.Variant.YELLOW_BLUE;
            }
            if (profile.id() == Minecraft.getInstance().player.getUUID()) {
                Map<@NotNull Shoulder, Parrot.@Nullable Variant> parrotMap = NopoMod.config.getParrotConfig().getParrots();
                if (parrotMap == null) return;
                if (parrotMap.get(Shoulder.LEFT) != null) {
                    avatarRenderState.parrotOnLeftShoulder = parrotMap.get(Shoulder.LEFT);
                }
                if (parrotMap.get(Shoulder.RIGHT) != null) {
                    avatarRenderState.parrotOnRightShoulder = parrotMap.get(Shoulder.RIGHT);
                }
            }
        }
    }
}
