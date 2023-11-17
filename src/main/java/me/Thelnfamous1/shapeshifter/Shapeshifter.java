package me.Thelnfamous1.shapeshifter;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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

    public Shapeshifter() {
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickEntity);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickItem);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
    }


    private void onRightClickEntity(PlayerInteractEvent.EntityInteract event){
        if(event.getItemStack().is(MORPH_REMOTE.get()) && !event.getEntity().isSecondaryUseActive()){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if(!event.getLevel().isClientSide){
                ((EntityDisguise)event.getEntity()).disguiseAs(event.getTarget().getType());
            }
        }
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event){
        if(event.getItemStack().is(MORPH_REMOTE.get())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if(!event.getLevel().isClientSide){
                Player player = event.getEntity();
                if(!player.isSecondaryUseActive()){
                    ((EntityDisguise) player).removeDisguise();
                } else if(((EntityDisguise) player).isDisguised()){
                    Entity disguiseEntity = ((EntityDisguise) player).getDisguiseEntity();
                    if(disguiseEntity instanceof DummyBlockEntity dummyBlock){
                        dummyBlock.cycleBlockState();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SYNC_CHANNEL.registerMessage(0, S2CUpdateDummyBlock.class, S2CUpdateDummyBlock::encode, S2CUpdateDummyBlock::new, S2CUpdateDummyBlock::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
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
