package me.Thelnfamous1.shapeshifter;

import com.mojang.logging.LogUtils;
import me.Thelnfamous1.shapeshifter.mixin.EntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

import java.util.Optional;

@Mod(Shapeshifter.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Shapeshifter {
    public static final String MODID = "shapeshifter";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MORPH_REMOTE = ITEMS.register("morph_remote", () -> new MorphRemoteItem(new Item.Properties()));

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<DummyBlockEntity>> DUMMY_BLOCK = ENTITY_TYPES.register("dummy_block", () ->
            EntityType.Builder.<DummyBlockEntity>of(DummyBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(new ResourceLocation(MODID, "dummy_block").toString()));

    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(MODID, "sync_channel");
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel SYNC_CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME, () -> "1.0",
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean UPDATE_DISGUISE = true;

    public Shapeshifter() {
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickEntity);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::onStartTracking);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
    }


    private void onRightClickEntity(PlayerInteractEvent.EntityInteract event){
        if(event.getItemStack().is(MORPH_REMOTE.get()) && !event.getEntity().isSecondaryUseActive()){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if(!event.getLevel().isClientSide){
                Player player = event.getEntity();
                EntityDisguise disguisedPlayer = (EntityDisguise) player;
                if(disguisedPlayer.getDisguiseEntity() == event.getTarget()) return;
                disguisedPlayer.disguiseAs(event.getTarget().getType());
                CompoundTag data = saveEntityData(event.getTarget());
                ((EntityAccessor) disguisedPlayer.getDisguiseEntity()).shapeshifter_callReadAdditionalSaveData(data);
                updateDisguise(disguisedPlayer, PacketDistributor.ALL.noArg(), data);
            }
        }
    }

    private static CompoundTag saveEntityData(Entity entity) {
        CompoundTag data = new CompoundTag();
        ((EntityAccessor) entity).shapeshifter_callAddAdditionalSaveData(data);
        return data;
    }

    public static void updateDisguise(EntityDisguise disguised, PacketDistributor.PacketTarget target){
        updateDisguise(disguised, target, null);
    }

    public static void updateDisguise(EntityDisguise disguised, PacketDistributor.PacketTarget target, @Nullable CompoundTag data) {
        if(UPDATE_DISGUISE && disguised.isDisguised()){
            Entity disguiseEntity = disguised.getDisguiseEntity();
            Shapeshifter.SYNC_CHANNEL.send(target, data == null ? new S2CUpdateDisguise(disguiseEntity) : new S2CUpdateDisguise(disguiseEntity, data));
        }
    }


    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        if(event.getItemStack().is(MORPH_REMOTE.get()) && !event.getEntity().isSecondaryUseActive()){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if(!event.getLevel().isClientSide){
                BlockPos clickedPos = event.getPos();
                BlockState state = event.getLevel().getBlockState(clickedPos);
                if(!state.isAir()){
                    Player player = event.getEntity();
                    EntityDisguise disguisedPlayer = (EntityDisguise) player;
                    DummyBlockEntity dummyBlock = new DummyBlockEntity(event.getLevel(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), state);
                    disguisedPlayer.disguiseAs(dummyBlock);
                    updateDisguise(disguisedPlayer, PacketDistributor.ALL.noArg());
                }
            }
        }
    }

    private static CompoundTag saveBlockData(BlockState state) {
        CompoundTag data = new CompoundTag();
        DummyBlockEntity.writeBlockState(data, state);
        return data;
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event){
        if(event.getItemStack().is(MORPH_REMOTE.get())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if(!event.getLevel().isClientSide){
                Player player = event.getEntity();
                EntityDisguise disguisedPlayer = (EntityDisguise) player;
                if(!player.isSecondaryUseActive()){
                    disguisedPlayer.removeDisguise();
                } else if(disguisedPlayer.isDisguised()){
                    Entity disguiseEntity = disguisedPlayer.getDisguiseEntity();
                    if(disguiseEntity instanceof DummyBlockEntity dummyBlock){
                        dummyBlock.cycleBlockState();
                        updateDisguise(disguisedPlayer, PacketDistributor.ALL.noArg());
                    }
                }
            }
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if(!event.getEntity().level.isClientSide){
            updateDisguise((EntityDisguise) event.getEntity(), PacketDistributor.ALL.noArg());
        }
    }

    private void onStartTracking(PlayerEvent.StartTracking event){
        if(!event.getTarget().level.isClientSide){
            updateDisguise((EntityDisguise) event.getTarget(), PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SYNC_CHANNEL.registerMessage(0, S2CUpdateDisguise.class, S2CUpdateDisguise::encode, S2CUpdateDisguise::new, S2CUpdateDisguise::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        });
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(DUMMY_BLOCK.get(), DummyBlockRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }
}
