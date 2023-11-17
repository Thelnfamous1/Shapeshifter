package me.Thelnfamous1.shapeshifter;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Shapeshifter.MODID)
public class DatagenHandler {

    @SubscribeEvent
    static void onGatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        LanguageProvider languageProvider = new LanguageProvider(generator, Shapeshifter.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                this.add(Shapeshifter.MORPH_REMOTE.get(), "Morph Remote");
                this.add(MorphRemoteItem.INFO_KEY, "Right-click on any block or entity to shapeshift into it!");
                this.add(Shapeshifter.DUMMY_BLOCK.get(), "Dummy Block");
            }
        };
        generator.addProvider(event.includeClient(), languageProvider);

        existingFileHelper.trackGenerated(new ResourceLocation(Shapeshifter.MODID, "item/morph_remote"), new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures"));

        ItemModelProvider itemModelProvider = new ItemModelProvider(generator, Shapeshifter.MODID, existingFileHelper) {
            @Override
            protected void registerModels() {
                this.basicItem(Shapeshifter.MORPH_REMOTE.get());
            }
        };
        generator.addProvider(event.includeClient(), itemModelProvider);


    }
}
