package me.Thelnfamous1.shapeshifter;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.phys.Vec3;

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
        writeBlockState(pCompound, this.getBlockState());
    }

    public static void writeBlockState(CompoundTag pCompound, BlockState blockState) {
        pCompound.put("BlockState", NbtUtils.writeBlockState(blockState));
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
        BlockState next = getNextBlockState(this.getBlockState());
        this.setBlockState(next);
    }

    public static BlockState getNextBlockState(BlockState blockState) {
        ImmutableList<BlockState> possibleStates = blockState.getBlock().getStateDefinition().getPossibleStates();
        int currentIdx = possibleStates.indexOf(blockState);
        return possibleStates.get((currentIdx + 1) % possibleStates.size());
    }
}
