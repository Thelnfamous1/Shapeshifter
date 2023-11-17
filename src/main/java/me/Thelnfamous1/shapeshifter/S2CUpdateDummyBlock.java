package me.Thelnfamous1.shapeshifter;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateDummyBlock {

    private final int id;
    private final BlockState blockState;

    public S2CUpdateDummyBlock(DummyBlockEntity dummyBlock){
        this.id = dummyBlock.getId();
        this.blockState = dummyBlock.getBlockState();
    }

    public S2CUpdateDummyBlock(FriendlyByteBuf buf){
        this.id = buf.readVarInt();
        this.blockState = Block.stateById(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeVarInt(this.id);
        buf.writeVarInt(Block.getId(this.blockState));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.id);
            if(entity instanceof DummyBlockEntity dummyBlock){
                dummyBlock.setBlockState(this.blockState);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
