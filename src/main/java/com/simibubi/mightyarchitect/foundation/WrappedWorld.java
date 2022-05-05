package com.simibubi.mightyarchitect.foundation;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.registries.tags.ITagManager;

public class WrappedWorld extends Level {

	protected Level world;

	protected LevelEntityGetter<Entity> entityGetter = new LevelEntityGetter<Entity>() {
		@Nullable
		@Override
		public Entity get(int p_156931_) {
			return null;
		}

		@Nullable
		@Override
		public Entity get(UUID p_156939_) {
			return null;
		}

		@Override
		public Iterable<Entity> getAll() {
			return Collections.emptyList();
		}

		@Override
		public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156935_, Consumer<U> p_156936_) {

		}

		@Override
		public void get(AABB p_156937_, Consumer<Entity> p_156938_) {

		}

		@Override
		public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156932_, AABB p_156933_, Consumer<U> p_156934_) {

		}
	};

	public WrappedWorld(Level world) {
		super((WritableLevelData) world.getLevelData(), world.dimension(), world.dimensionTypeRegistration(),
			() -> world.getProfiler(), world.isClientSide, false, 0);
		this.world = world;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
		return world.isStateAtPosition(p_217375_1_, p_217375_2_);
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		return world.setBlock(pos, newState, flags);
	}

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		world.sendBlockUpdated(pos, oldState, newState, flags);
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return world.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return world.getFluidTicks();
	}

	@Override
	public void levelEvent(Player player, int type, BlockPos pos, int data) {}

	@Override
	public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category,
		float volume, float pitch) {}

	@Override
	public void playSound(Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
		SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public String gatherChunkSourceStats() {
		return null;
	}

	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	public MapItemSavedData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		entityIn.level = world;
		return world.addFreshEntity(entityIn);
	}

	@Override
	public void setMapData(String mapId, MapItemSavedData mapDataIn) {}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	@Override
	public ChunkSource getChunkSource() {
		return world.getChunkSource();
	}

	@Override
	public RegistryAccess registryAccess() {
		return world.registryAccess();
	}

	@Override
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return 1;
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	public Level getWorld() {
		return world;
	}

}
