package com.simibubi.mightyarchitect.foundation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SuperByteBufferCache {

	public static class Compartment<T> {
	}

	public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
	Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

	public SuperByteBufferCache() {
		cache = new HashMap<>();
		registerCompartment(GENERIC_TILE);
	}

	public SuperByteBuffer renderBlock(BlockState toRender) {
		return getGeneric(toRender, () -> standardBlockRender(toRender));
	}

	public SuperByteBuffer renderBlockIn(Compartment<BlockState> compartment, BlockState toRender) {
		return get(compartment, toRender, () -> standardBlockRender(toRender));
	}

	SuperByteBuffer getGeneric(BlockState key, Supplier<SuperByteBuffer> supplier) {
		return get(GENERIC_TILE, key, supplier);
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Supplier<SuperByteBuffer> supplier) {
		Cache<Object, SuperByteBuffer> compartmentCache = this.cache.get(compartment);
		try {
			return compartmentCache.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void registerCompartment(Compartment<?> instance) {
		cache.put(instance, CacheBuilder.newBuilder().build());
	}

	public void registerCompartment(Compartment<?> instance, long ticksTillExpired) {
		cache.put(instance,
				CacheBuilder.newBuilder().expireAfterAccess(ticksTillExpired * 50, TimeUnit.MILLISECONDS).build());
	}

	private SuperByteBuffer standardBlockRender(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		return standardModelRender(dispatcher.getBlockModel(renderedState), renderedState);
	}

	private SuperByteBuffer standardModelRender(IBakedModel model, BlockState referenceState) {
		return standardModelRender(model, referenceState, new MatrixStack());
	}

	private SuperByteBuffer standardModelRender(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockModelRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		Random random = new Random();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModelFlat(Minecraft.getInstance().level, model, referenceState, BlockPos.ZERO.above(255), ms,
				builder, true, random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
		builder.end();

		return new SuperByteBuffer(builder);
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> {
			cache.invalidateAll();
		});
	}

}
