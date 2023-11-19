package me.Thelnfamous1.shapeshifter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MorphRemoteItem extends Item {

    public static final String INFO_KEY = "item.shapeshifter.morph_remote.info";
    public static final String BLOCK_KEY = "item.shapeshifter.morph_remote.block";

    public MorphRemoteItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable(INFO_KEY).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable(BLOCK_KEY).withStyle(ChatFormatting.GRAY));
    }
}
