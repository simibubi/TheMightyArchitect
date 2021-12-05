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
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.core.BlockPos;
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
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		return standardModelRender(dispatcher.getBlockModel(renderedState), renderedState);
	}

	private SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState) {
		return standardModelRender(model, referenceState, new PoseStack());
	}

	private SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState, PoseStack ms) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormat.BLOCK.getIntegerSize());
		Random random = new Random();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.tesselateBlock(Minecraft.getInstance().level, model, referenceState, BlockPos.ZERO.above(255), ms,
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
