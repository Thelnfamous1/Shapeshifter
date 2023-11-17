package me.Thelnfamous1.shapeshifter;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class DummyBlockEntity extends Entity {
    protected static final EntityDataAccessor<Optional<BlockState>> DATA_BLOCK_STATE = SynchedEntityData.defineId(DummyBlockEntity.class, EntityDataSerializers.BLOCK_STATE);
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(DummyBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    public DummyBlockEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public DummyBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        this(Shapeshifter.DUMMY_BLOCK.get(), pLevel);
        this.setBlockState(pState);
        this.blocksBuilding = true;
        this.setPos(pX, pY, pZ);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = pX;
        this.yo = pY;
        this.zo = pZ;
        this.setStartPos(this.blockPosition());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BLOCK_STATE, Optional.empty());
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos pStartPos) {
        this.entityData.set(DATA_START_POS, pStartPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE).orElse(Blocks.SAND.defaultBlockState());
    }

    public void setBlockState(BlockState blockState){
        this.entityData.set(DATA_BLOCK_STATE, Optional.of(blockState));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.setBlockState(NbtUtils.readBlockState(pCompound.getCompound("BlockState")));
        if (this.getBlockState().isAir()) {
            this.setBlockState(Blocks.SAND.defaultBlockState());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.put("BlockState", NbtUtils.writeBlockState(this.getBlockState()));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setBlockState(Block.stateById(pPacket.getData()));
        this.blocksBuilding = true;
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        this.setPos(d0, d1, d2);
        this.setStartPos(this.blockPosition());
    }

    public void cycleBlockState() {
        if(this.getBlockState().hasProperty(BlockStateProperties.FACING)){
            this.cycleProperty(BlockStateProperties.FACING);
        } else if(this.getBlockState().hasProperty(BlockStateProperties.FACING_HOPPER)){
            this.cycleProperty(BlockStateProperties.FACING_HOPPER);
        } else if(this.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            this.cycleProperty(BlockStateProperties.HORIZONTAL_FACING);
        } else if(this.getBlockState().hasProperty(BlockStateProperties.AXIS)){
            this.cycleProperty(BlockStateProperties.AXIS);
        } else if(this.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_AXIS)){
            this.cycleProperty(BlockStateProperties.HORIZONTAL_AXIS);
        }
    }

    private <T extends Comparable<T>> void cycleProperty(Property<T> property){
        this.setBlockState(this.getBlockState().cycle(property));
        this.sendUpdatePacket();
    }

    private void sendUpdatePacket() {
        Shapeshifter.SYNC_CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateDummyBlock(this));
    }
}
