package com.nopo.mixin;

import at.hannibal2.skyhanni.features.chat.CopyChat;
import at.hannibal2.skyhanni.utils.StringUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Pseudo
@Mixin(CopyChat.class)
public class MixinCopyChat {

    @Unique
    Pattern spriteRegex = Pattern.compile("\\[nopo:[a-zA-Z0-9_]+@[a-zA-Z0-9_]+]");

    @WrapOperation(method = "processCopyChat", at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/StringUtils;removeColor$default(Lat/hannibal2/skyhanni/utils/StringUtils;Ljava/lang/CharSequence;ZILjava/lang/Object;)Ljava/lang/String;"))
    public String modifyCopyChat(StringUtils stringUtils, CharSequence charSequence, boolean b, int i, Object o, Operation<String> original) {
        String message = original.call(stringUtils, charSequence, b, i, o);
        Matcher matcher = spriteRegex.matcher(message);
        return matcher.replaceAll("");
    }
}
