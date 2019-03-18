package com.simibubi.mightyarchitect.buildomatico.client;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.model.Schematic;
import com.simibubi.mightyarchitect.buildomatico.model.template.TemplateBlockAccess;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Renders a structure into the world
 * @author simibubi
 */
@EventBusSubscriber(Side.CLIENT)
public class SchematicHologram {

	// These buffers are large enough for an entire chunk, consider using
	// smaller buffers
	private static final RegionRenderCacheBuilder bufferCache = new RegionRenderCacheBuilder();
	private static final boolean[] usedBlockRenderLayers = new boolean[BlockRenderLayer.values().length];
	private static final boolean[] startedBufferBuilders = new boolean[BlockRenderLayer.values().length];

	private static SchematicHologram instance;
	private boolean active;
	private boolean changed;
	private Schematic schematic;

	public SchematicHologram() {
		instance = this;
		changed = false;
	}

	public void startHologram(Schematic schematic) {
		this.schematic = schematic;
		active = true;
		changed = true;
	}

	public static SchematicHologram getInstance() {
		return instance;
	}
	
	public static void display(Schematic schematic) {
		instance = new SchematicHologram();
		instance.startHologram(schematic);
	}

	public static void reset() {
		instance = null;
	}
	
	public void schematicChanged() {
		changed = true;
	}

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {
		if (instance != null && instance.active) {
			final Minecraft minecraft = Minecraft.getMinecraft();
			if (event.phase != TickEvent.Phase.END)
				return;
			if (minecraft.world == null)
				return;
			if (minecraft.player == null)
				return;
			if (instance.changed) {
				redraw(minecraft);
				instance.changed = false;
			}
		}
	}

	private static void redraw(final Minecraft minecraft) {
		Arrays.fill(usedBlockRenderLayers, false);
		Arrays.fill(startedBufferBuilders, false);
		
		Schematic schematic = instance.schematic;
		TemplateBlockAccess materializedSketch = (TemplateBlockAccess) schematic.getMaterializedSketch();
		
		final IBlockAccess blockAccess = materializedSketch;
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		List<IBlockState> blockstates = new LinkedList<>();
		
		for (BlockPos localPos : materializedSketch.getAllPositions()) {
			BlockPos pos = localPos.add(schematic.getAnchor());
			final IBlockState state = blockAccess.getBlockState(pos);
			for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!state.getBlock().canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				ForgeHooksClient.setRenderLayer(blockRenderLayer);
				final int blockRenderLayerId = blockRenderLayer.ordinal();
				final BufferBuilder bufferBuilder = bufferCache.getWorldRendererByLayerId(blockRenderLayerId);
				if (!startedBufferBuilders[blockRenderLayerId]) {
					startedBufferBuilders[blockRenderLayerId] = true;
					// Copied from RenderChunk
					{
						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					}
				}
				// OptiFine Shaders compatibility
				// if (Config.isShaders()) SVertexBuilder.pushEntity(state, pos,
				// blockAccess, bufferBuilder);
				usedBlockRenderLayers[blockRenderLayerId] |= blockRendererDispatcher.renderBlock(state, pos,
						blockAccess, bufferBuilder);
				blockstates.add(state);
				// if (Config.isShaders())
				// SVertexBuilder.popEntity(bufferBuilder);
			}
			ForgeHooksClient.setRenderLayer(null);
		}

		// finishDrawing
		for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
			if (!startedBufferBuilders[blockRenderLayerId]) {
				continue;
			}
			bufferCache.getWorldRendererByLayerId(blockRenderLayerId).finishDrawing();
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (instance != null && instance.active) {
			final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
			
			if (entity == null) {
				return;
			}
			
			final float partialTicks = event.getPartialTicks();
			
			// Copied from EntityRenderer. This code can be found by looking at usages of Entity.prevPosX.
			// It also appears in many other places throughout Minecraft's rendering
			double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
			double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
			double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
	
			for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
				if (!usedBlockRenderLayers[blockRenderLayerId]) {
					continue;
				}
				final BufferBuilder bufferBuilder = bufferCache.getWorldRendererByLayerId(blockRenderLayerId);
				GlStateManager.pushMatrix();
				GlStateManager.translate(-renderPosX, -renderPosY, -renderPosZ);
				drawBuffer(bufferBuilder);
				GlStateManager.popMatrix();
			}
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
		}
	}

	// Coppied from the Tesselator's vboUploader - Draw everything but don't
	// reset the buffer
	private static void drawBuffer(final BufferBuilder bufferBuilder) {
		if (bufferBuilder.getVertexCount() > 0) {
			
			VertexFormat vertexformat = bufferBuilder.getVertexFormat();
			int size = vertexformat.getNextOffset();
			ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int index = 0; index < list.size(); ++index) {
				VertexFormatElement vertexformatelement = list.get(index);
				VertexFormatElement.EnumUsage usage = vertexformatelement.getUsage();
				bytebuffer.position(vertexformat.getOffset(index));
				usage.preDraw(vertexformat, index, size, bytebuffer);
			}

			GlStateManager.glDrawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
			
			for (int index = 0; index < list.size(); ++index) {
				VertexFormatElement vertexformatelement = list.get(index);
				VertexFormatElement.EnumUsage usage = vertexformatelement.getUsage();
				usage.postDraw(vertexformat, index, size, bytebuffer);
			}
		}
	}

}
