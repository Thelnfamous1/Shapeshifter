package me.Thelnfamous1.shapeshifter.mixin;

import me.Thelnfamous1.shapeshifter.DummyBlockEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;
import xyz.nucleoid.disguiselib.impl.mixin.accessor.EntitySpawnS2CPacketAccessor;
import xyz.nucleoid.disguiselib.impl.packets.FakePackets;

@Mixin(value = FakePackets.class, remap = false)
public class FakePacketsMixin {

    @Inject(method = "universalSpawnPacket", at = @At("HEAD"), cancellable = true)
    private static void handleNonLivingDisguise(Entity entity, CallbackInfoReturnable<Packet<?>> cir){
        Entity disguise = ((EntityDisguise)entity).getDisguiseEntity();
        if (disguise != null && !(disguise instanceof LivingEntity)) {
            Packet<?> packet = disguise.getAddEntityPacket();
            if (packet instanceof ClientboundAddEntityPacket) {
                packet = FakePackets.fakeEntitySpawnS2CPacket(entity);
                if (disguise instanceof DummyBlockEntity) {
                    ((EntitySpawnS2CPacketAccessor)packet).setEntityData(Block.getId(((DummyBlockEntity)disguise).getBlockState()));
                }
                cir.setReturnValue(packet);
            }
        }
    }
}
