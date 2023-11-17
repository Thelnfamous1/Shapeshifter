package me.Thelnfamous1.shapeshifter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class DummyBlockRenderer extends EntityRenderer<DummyBlockEntity> {
   private final BlockRenderDispatcher dispatcher;

   public DummyBlockRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.shadowRadius = 0.5F;
      this.dispatcher = pContext.getBlockRenderDispatcher();
   }

   @Override
   public void render(DummyBlockEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
      BlockState blockState = pEntity.getBlockState();
      if (blockState.getRenderShape() == RenderShape.MODEL) {
         Level level = pEntity.getLevel();
         if (blockState != level.getBlockState(pEntity.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
            pMatrixStack.pushPose();
            BlockPos blockpos = new BlockPos(pEntity.getX(), pEntity.getBoundingBox().maxY, pEntity.getZ());
            pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
            var model = this.dispatcher.getBlockModel(blockState);
            for (var renderType : model.getRenderTypes(blockState, RandomSource.create(blockState.getSeed(pEntity.getStartPos())), net.minecraftforge.client.model.data.ModelData.EMPTY))
               this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockState, blockpos, pMatrixStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), blockState.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, renderType);
            pMatrixStack.popPose();
            super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
         }
      }
   }

   @Override
   public ResourceLocation getTextureLocation(DummyBlockEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}