package dev.chestunlocker.mixin;

import dev.chestunlocker.ChestUnlockerClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleParticleEvent", at = @At("HEAD"))
    private void chestunlocker$onParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        ChestUnlockerClient.onParticle(packet);
    }
}
