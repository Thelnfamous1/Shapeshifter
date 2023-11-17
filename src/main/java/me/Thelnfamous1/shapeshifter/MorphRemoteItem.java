package me.Thelnfamous1.shapeshifter;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

import java.util.List;

public class MorphRemoteItem extends Item {

    public static final String INFO_KEY = "item.shapeshifter.morph_remote.info";

    public MorphRemoteItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(pContext.getPlayer() != null && pContext.getPlayer().isSecondaryUseActive()){
            return InteractionResult.PASS;
        }

        Level level = pContext.getLevel();
        if(!level.isClientSide){
            Player player = pContext.getPlayer();
            if(player != null){
                BlockPos clickedPos = pContext.getClickedPos();
                BlockState state = pContext.getLevel().getBlockState(clickedPos);
                if(!state.isAir()){
                    DummyBlockEntity dummyBlock = new DummyBlockEntity(level, clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), state);
                    ((EntityDisguise)player).disguiseAs(dummyBlock);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable(INFO_KEY).withStyle(ChatFormatting.GRAY));
    }
}
