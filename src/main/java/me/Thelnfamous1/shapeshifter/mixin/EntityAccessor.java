package me.Thelnfamous1.shapeshifter.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("addAdditionalSaveData")
    void shapeshifter_callAddAdditionalSaveData(CompoundTag tag);

    @Invoker("readAdditionalSaveData")
    void shapeshifter_callReadAdditionalSaveData(CompoundTag tag);

}
