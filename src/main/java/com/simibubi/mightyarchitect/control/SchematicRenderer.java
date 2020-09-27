package com.simibubi.mightyarchitect.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.foundation.MatrixStacker;
import com.simibubi.mightyarchitect.foundation.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicRenderer {

	private final Map<RenderType, SuperByteBuffer> bufferCache = new HashMap<>(getLayerCount());
	private final Set<RenderType> usedBlockRenderLayers = new HashSet<>(getLayerCount());
	private final Set<RenderType> startedBufferBuilders = new HashSet<>(getLayerCount());
	private boolean active;
	private boolean changed;
	private Schematic schematic;
	private BlockPos anchor;

	public SchematicRenderer() {
		changed = false;
	}

	public void display(Schematic schematic) {
		this.anchor = schematic.getAnchor();
		this.schematic = schematic;
		this.active = true;
		this.changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void tick() {
		if (!active)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.world == null || mc.player == null || !changed)
			return;

		redraw(mc);
		changed = false;
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		if (!active)
			return;
		ms.push();
		ms.translate(anchor.getX(), anchor.getY(), anchor.getZ());
		buffer.getBuffer(RenderType.getSolid());
		for (RenderType layer : RenderType.getBlockLayers()) {
			if (!usedBlockRenderLayers.contains(layer))
				continue;
			SuperByteBuffer superByteBuffer = bufferCache.get(layer);
			superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
		}
		ms.pop();
	}

	private void redraw(Minecraft minecraft) {
		usedBlockRenderLayers.clear();
		startedBufferBuilders.clear();

		final ILightReader blockAccess = schematic.getMaterializedSketch();
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		List<BlockState> blockstates = new LinkedList<>();
		Map<RenderType, BufferBuilder> buffers = new HashMap<>();
		MatrixStack ms = new MatrixStack();

		BlockPos.func_229383_a_(schematic.getLocalBounds().toMBB())
			.forEach(localPos -> {
				ms.push();
				MatrixStacker.of(ms)
					.translate(localPos);
				BlockPos pos = localPos.add(anchor);
				BlockState state = blockAccess.getBlockState(pos);

				for (RenderType blockRenderLayer : RenderType.getBlockLayers()) {
					if (!RenderTypeLookup.canRenderInLayer(state, blockRenderLayer))
						continue;
					ForgeHooksClient.setRenderLayer(blockRenderLayer);
					if (!buffers.containsKey(blockRenderLayer))
						buffers.put(blockRenderLayer, new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize()));

					BufferBuilder bufferBuilder = buffers.get(blockRenderLayer);
					if (startedBufferBuilders.add(blockRenderLayer))
						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					if (blockRendererDispatcher.renderModel(state, pos, blockAccess, ms, bufferBuilder, true,
						minecraft.world.rand, EmptyModelData.INSTANCE)) {
						usedBlockRenderLayers.add(blockRenderLayer);
					}
					blockstates.add(state);
				}

				ForgeHooksClient.setRenderLayer(null);
				ms.pop();
			});

		// finishDrawing
		for (RenderType layer : RenderType.getBlockLayers()) {
			if (!startedBufferBuilders.contains(layer))
				continue;
			BufferBuilder buf = buffers.get(layer);
			buf.finishDrawing();
			bufferCache.put(layer, new SuperByteBuffer(buf));
		}
	}

	private static int getLayerCount() {
		return RenderType.getBlockLayers()
			.size();
	}

}
