package net.warze.hspcolor.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.warze.hspcolor.HspColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @ModifyArg(
        method = "onGameMessage(Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/message/MessageHandler;onGameMessage(Lnet/minecraft/text/Text;Z)V"
        ),
        index = 0
    )
    private Text hspcolor$rewriteGuildChat(Text original) {
        MutableText mutable = original.copy();
        return HspColor.instance().chatInterceptor().rewrite(mutable);
    }
}
