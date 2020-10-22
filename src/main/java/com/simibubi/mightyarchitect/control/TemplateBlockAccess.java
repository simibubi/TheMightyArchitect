package com.simibubi.mightyarchitect.control;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.foundation.WrappedWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap.Type;

public class TemplateBlockAccess extends WrappedWorld {

	private Map<BlockPos, BlockState> blocks;
	private Cuboid bounds;
	private BlockPos anchor;
	private boolean localMode;

	public TemplateBlockAccess(Map<BlockPos, BlockState> blocks, Cuboid bounds, BlockPos anchor) {
		super(Minecraft.getInstance().world);
		this.blocks = blocks;
		this.bounds = bounds;
		this.anchor = anchor;
		updateBlockstates();
	}

	public void localMode(boolean local) {
		this.localMode = local;
	}

	private void updateBlockstates() {
		Set<BlockPos> keySet = new HashSet<>(blocks.keySet());
		keySet.forEach(pos -> {
			BlockState blockState = blocks.get(pos);
			if (blockState == null)
				return;
			blockState.updateNeighbors(this, pos.add(anchor), 16);
		});
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = localMode ? globalPos : globalPos.subtract(anchor);
		if (getBounds().contains(pos) && blocks.containsKey(pos)) {
			return blocks.get(pos);
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return getRegistryManager().get(Registry.BIOME_KEY)
			.get(Biomes.THE_VOID);
	}

	@Override
	public int getLight(BlockPos p_201696_1_) {
		return 0xF;
	}

	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity arg0, AxisAlignedBB arg1, Predicate<? super Entity> arg2) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> arg0, AxisAlignedBB arg1,
		Predicate<? super T> arg2) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public int getLightLevel(LightType lt, BlockPos p_226658_2_) {
		return lt == LightType.BLOCK ? 12 : 14;
	}

	@Override
	public int getLightValue(BlockPos pos) {
		return super.getLightValue(pos);
	}

	@Override
	public BlockPos getHeight(Type heightmapType, BlockPos pos) {
		return BlockPos.ZERO;
	}

	@Override
	public int getHeight(Type heightmapType, int x, int z) {
		return 256;
	}

	@Override
	public boolean hasBlockState(BlockPos pos, Predicate<BlockState> predicate) {
		return predicate.test(getBlockState(pos));
	}

	@Override
	public boolean destroyBlock(BlockPos arg0, boolean arg1) {
		return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
	}

	@Override
	public boolean removeBlock(BlockPos arg0, boolean arg1) {
		return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int p_241211_3_, int p_241211_4_) {
		blocks.put(localMode ? pos : pos.subtract(anchor), state);
		return true;
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return EmptyTickList.get();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return EmptyTickList.get();
	}

	@Override
	public Random getRandom() {
		return new Random();
	}

	@Override
	public void notifyNeighborsOfStateChange(BlockPos p_195593_1_, Block p_195593_2_) {}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

	@Override
	public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume,
		float pitch) {}

	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed,
		double zSpeed) {}

	@Override
	public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {}

	public Cuboid getBounds() {
		return bounds;
	}

}
