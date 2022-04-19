package tudbut.mod.polyfire.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tudbut.mod.polyfire.events.EventHandler;

@Mixin(NetworkManager.class)
public class MixinClientConnection {
    
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        if(EventHandler.onPacket(packet))
            callbackInfo.cancel();
    }
    
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void channelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        if(EventHandler.onPacket(packet))
            callbackInfo.cancel();
    }
}
