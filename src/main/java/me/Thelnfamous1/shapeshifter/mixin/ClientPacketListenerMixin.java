package me.Thelnfamous1.shapeshifter.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    /**
     * Fixes crashes when the sender is disguised as a non-living entity
     */
    @ModifyVariable(method = "handleAnimate", at = @At(value = "LOAD", ordinal = 0), ordinal = 0)
    private Entity modifyAnimateEntity(Entity original, ClientboundAnimatePacket packet){
        switch (packet.getAction()) {
            case 0, 2, 3 -> {
                if (!(original instanceof LivingEntity)) return null;
            }
        }

        return original;
    }
}
