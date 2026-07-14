package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nopo.NopoMod;
import com.nopo.events.ModifyOutgoingMessages;
import com.nopo.module.BaseModule;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @WrapMethod(method = "handleChatInput")
    public void modifyChatSend(String string, boolean bl, Operation<Void> original) {
        String newMessage = string;
        for (BaseModule module : NopoMod.INSTANCE.getModules()) {
            if (module instanceof ModifyOutgoingMessages event) {
                newMessage = event.onChatSent(newMessage);
            }
        }
        original.call(newMessage, bl);
    }
}
