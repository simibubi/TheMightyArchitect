package com.simibubi.mightyarchitect.foundation;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;

public class WrappedWorld extends World {

	protected World world;

	public WrappedWorld(World world) {
		super((ISpawnWorldInfo) world.getWorldInfo(), world.getRegistryKey(), world.getDimension(),
			() -> world.getProfiler(), world.isRemote, false, 0);
		this.world = world;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
		return world.hasBlockState(p_217375_1_, p_217375_2_);
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(pos);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
		return world.setBlockState(pos, newState, flags);
	}

	@Override
	public int getLight(BlockPos pos) {
		return 15;
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		world.notifyBlockUpdate(pos, oldState, newState, flags);
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return world.getPendingBlockTicks();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return world.getPendingFluidTicks();
	}

	@Override
	public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
		float volume, float pitch) {}

	@Override
	public void playMovingSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
		SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean addEntity(Entity entityIn) {
		entityIn.setWorld(world);
		return world.addEntity(entityIn);
	}

	@Override
	public void registerMapData(MapData mapDataIn) {}

	@Override
	public int getNextMapId() {
		return 0;
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public Biome getGeneratorStoredBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getGeneratorStoredBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	@Override
	public AbstractChunkProvider getChunkProvider() {
		return world.getChunkProvider();
	}

	@Override
	public DynamicRegistries getRegistryManager() {
		return world.getRegistryManager();
	}

	@Override
	public float getBrightness(Direction p_230487_1_, boolean p_230487_2_) {
		return 1;
	}

	@Override
	public ITagCollectionSupplier getTags() {
		return world.getTags();
	}

	public World getWorld() {
		return world;
	}

}
