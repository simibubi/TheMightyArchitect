package com.simibubi.mightyarchitect.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.mightyarchitect.foundation.WrappedWorld;
import com.simibubi.mightyarchitect.foundation.utility.AngleHelper;
import com.simibubi.mightyarchitect.foundation.utility.ColorHelper;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GuiGameElement {

	public static GuiRenderBuilder of(ItemStack stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(IItemProvider itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState state) {
		return new GuiBlockStateRenderBuilder(state);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockStateRenderBuilder(fluid.getDefaultState()
			.getBlockState()
			.with(FlowingFluidBlock.LEVEL, 0));
	}

	public static abstract class GuiRenderBuilder {
		double xBeforeScale, yBeforeScale, zBeforeScale = 0;
		double x, y, z;
		double xRot, yRot, zRot;
		double scale = 1;
		int color = 0xFFFFFF;
		Vector3d rotationOffset = Vector3d.ZERO;

		public GuiRenderBuilder atLocal(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public GuiRenderBuilder at(double x, double y) {
			this.xBeforeScale = x;
			this.yBeforeScale = y;
			return this;
		}

		public GuiRenderBuilder at(double x, double y, double z) {
			this.xBeforeScale = x;
			this.yBeforeScale = y;
			this.zBeforeScale = z;
			return this;
		}

		public GuiRenderBuilder rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public GuiRenderBuilder rotateBlock(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public GuiRenderBuilder scale(double scale) {
			this.scale = scale;
			return this;
		}

		public GuiRenderBuilder color(int color) {
			this.color = color;
			return this;
		}

		public GuiRenderBuilder withRotationOffset(Vector3d offset) {
			this.rotationOffset = offset;
			return this;
		}

		public abstract void render(MatrixStack matrixStack);

		@Deprecated
		protected void prepare() {}

		protected void prepareMatrix(MatrixStack matrixStack) {
			matrixStack.push();
			RenderSystem.enableBlend();
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableAlphaTest();
			RenderHelper.enableGuiDepthLighting();
			RenderSystem.alphaFunc(516, 0.1F);
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		@Deprecated
		protected void transform() {
			RenderSystem.translated(xBeforeScale, yBeforeScale, 0);
			RenderSystem.scaled(scale, scale, scale);
			RenderSystem.translated(x, y, z);
			RenderSystem.scaled(1, -1, 1);
			RenderSystem.translated(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			RenderSystem.rotatef((float) zRot, 0, 0, 1);
			RenderSystem.rotatef((float) xRot, 1, 0, 0);
			RenderSystem.rotatef((float) yRot, 0, 1, 0);
			RenderSystem.translated(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		protected void transformMatrix(MatrixStack matrixStack) {
			matrixStack.translate(xBeforeScale, yBeforeScale, zBeforeScale);
			matrixStack.scale((float) scale, (float) scale, (float) scale);
			matrixStack.translate(x, y, z);
			matrixStack.scale(1, -1, 1);
			matrixStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) zRot));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((float) xRot));
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) yRot));
			matrixStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		@Deprecated
		protected void cleanUp() {}

		protected void cleanUpMatrix(MatrixStack matrixStack) {
			matrixStack.pop();
			RenderSystem.disableAlphaTest();
			RenderSystem.disableRescaleNormal();
		}
	}

	private static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected IBakedModel blockmodel;
		protected BlockState blockState;

		public GuiBlockModelRenderBuilder(IBakedModel blockmodel, @Nullable BlockState blockState) {
			this.blockState = blockState == null ? Blocks.AIR.getDefaultState() : blockState;
			this.blockmodel = blockmodel;
		}

		@Override
		public void render(MatrixStack matrixStack) {
			prepareMatrix(matrixStack);

			Minecraft mc = Minecraft.getInstance();
			BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
			IRenderTypeBuffer.Impl buffer = mc.getBufferBuilders()
				.getEntityVertexConsumers();
			RenderType renderType = blockState.getBlock() == Blocks.AIR ? Atlases.getEntityTranslucentCull()
				: RenderTypeLookup.getEntityBlockLayer(blockState, true);
			IVertexBuilder vb = buffer.getBuffer(renderType);

			transformMatrix(matrixStack);

			mc.getTextureManager()
				.bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
			renderModel(blockRenderer, buffer, renderType, vb, matrixStack);

			cleanUpMatrix(matrixStack);
		}

		protected void renderModel(BlockRendererDispatcher blockRenderer, IRenderTypeBuffer.Impl buffer,
			RenderType renderType, IVertexBuilder vb, MatrixStack ms) {
			Vector3d rgb = ColorHelper.getRGB(color);
			blockRenderer.getBlockModelRenderer()
				.renderModel(ms.peek(), vb, blockState, blockmodel, (float) rgb.x, (float) rgb.y, (float) rgb.z,
					0xF000F0, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
			buffer.draw();
		}
	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockStateRenderBuilder(BlockState blockstate) {
			super(Minecraft.getInstance()
				.getBlockRendererDispatcher()
				.getModelForState(blockstate), blockstate);
		}

		@Override
		protected void renderModel(BlockRendererDispatcher blockRenderer, IRenderTypeBuffer.Impl buffer,
			RenderType renderType, IVertexBuilder vb, MatrixStack ms) {
			if (blockState.getBlock() instanceof FireBlock) {
				RenderHelper.disableGuiDepthLighting();
				blockRenderer.renderBlock(blockState, ms, buffer, 0xF000F0, OverlayTexture.DEFAULT_UV,
					EmptyModelData.INSTANCE);
				RenderHelper.enable();
				buffer.draw();
				return;
			}

			super.renderModel(blockRenderer, buffer, renderType, vb, ms);

			if (blockState.getFluidState()
				.isEmpty())
				return;

			for (RenderType type : RenderType.getBlockLayers()) {
				if (!RenderTypeLookup.canRenderInLayer(blockState.getFluidState(), type))
					continue;

				ms.push();
				RenderHelper.disableStandardItemLighting();

				ClientWorld world = Minecraft.getInstance().world;
				if (renderWorld == null || renderWorld.getWorld() != world)
					renderWorld = new FluidRenderWorld(world);

				for (Direction d : Iterate.directions) {
					vb = buffer.getBuffer(type);
					if (d.getAxisDirection() == AxisDirection.POSITIVE)
						continue;

					ms.push();
					ms.translate(.5, .5, .5);
					ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(AngleHelper.horizontalAngle(d)));
					ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(AngleHelper.verticalAngle(d) - 90));
					ms.translate(-.5, -.5, -.5);
					blockRenderer.renderFluid(new BlockPos(0, 1, 0), renderWorld, vb, blockState.getFluidState());
					buffer.draw(type);
					ms.pop();
				}

				RenderHelper.enable();
				ms.pop();
				break;
			}
		}
	}

	public static class GuiItemRenderBuilder extends GuiRenderBuilder {

		private final ItemStack stack;

		public GuiItemRenderBuilder(ItemStack stack) {
			this.stack = stack;
		}

		public GuiItemRenderBuilder(IItemProvider provider) {
			this(new ItemStack(provider));
		}

		@Override
		public void render(MatrixStack matrixStack) {
			prepareMatrix(matrixStack);
//			matrixStack.translate(0, 80, 0);
			transformMatrix(matrixStack);
			renderItemIntoGUI(matrixStack, stack);
			cleanUpMatrix(matrixStack);
		}

		public static void renderItemIntoGUI(MatrixStack matrixStack, ItemStack stack) {
			ItemRenderer renderer = Minecraft.getInstance()
				.getItemRenderer();
			IBakedModel bakedModel = renderer.getItemModelWithOverrides(stack, null, null);
			matrixStack.push();
			Minecraft.getInstance().textureManager.bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
			Minecraft.getInstance().textureManager.getTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE)
				.setBlurMipmapDirect(false, false);
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableAlphaTest();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.translate((float) 0, (float) 0, 100.0F + renderer.zLevel);
			matrixStack.translate(8.0F, 8.0F, 0.0F);
			matrixStack.scale(16.0F, 16.0F, 16.0F);
			IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance()
				.getBufferBuilders()
				.getEntityVertexConsumers();
			boolean flag = !bakedModel.isSideLit();
			if (flag) {
				RenderHelper.disableGuiDepthLighting();
			}

			renderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack,
				irendertypebuffer$impl, 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
			irendertypebuffer$impl.draw();
			RenderSystem.enableDepthTest();
			if (flag) {
				RenderHelper.enableGuiDepthLighting();
			}

			RenderSystem.disableAlphaTest();
			RenderSystem.disableRescaleNormal();
			RenderSystem.enableCull();
			matrixStack.pop();
		}

	}

	private static FluidRenderWorld renderWorld;

	private static class FluidRenderWorld extends WrappedWorld {

		public FluidRenderWorld(World world) {
			super(world);
		}

		@Override
		public int getLightLevel(@Nullable LightType p_226658_1_, @Nullable BlockPos p_226658_2_) {
			return 15;
		}

		@Override
		@Nonnull
		public BlockState getBlockState(BlockPos pos) {
			return Blocks.AIR.getDefaultState();
		}

	}
}
