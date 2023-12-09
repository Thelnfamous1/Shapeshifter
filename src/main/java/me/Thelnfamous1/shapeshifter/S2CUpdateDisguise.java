package me.Thelnfamous1.shapeshifter;

import me.Thelnfamous1.shapeshifter.mixin.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateDisguise {

    private final int id;
    private final EntityType<?> type;
    private final CompoundTag data;

    public S2CUpdateDisguise(Entity disguisedEntity, CompoundTag data){
        this.id = disguisedEntity.getId();
        this.type = disguisedEntity.getType();
        this.data = data;
    }

    public S2CUpdateDisguise(Entity disguisedEntity){
        this(disguisedEntity, new CompoundTag());
        ((EntityAccessor)disguisedEntity).shapeshifter_callAddAdditionalSaveData(this.data);
    }

    public S2CUpdateDisguise(FriendlyByteBuf buf){
        this.id = buf.readVarInt();
        this.type = buf.readById(Registry.ENTITY_TYPE);
        this.data = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeVarInt(this.id);
        buf.writeId(Registry.ENTITY_TYPE, this.type);
        buf.writeNbt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.id);
            if(entity.getType() == this.type){
                ((EntityAccessor)entity).shapeshifter_callReadAdditionalSaveData(this.data);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
