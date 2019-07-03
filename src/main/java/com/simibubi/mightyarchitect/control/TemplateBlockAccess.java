package com.simibubi.mightyarchitect.control;

import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.compose.Cuboid;

import net.minecraft.block.state.BlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class TemplateBlockAccess implements IBlockAccess {

	private Map<BlockPos, BlockState> blocks;
	private Cuboid bounds;
	private BlockPos anchor;
	
	public TemplateBlockAccess(Map<BlockPos, BlockState> blocks, Cuboid bounds, BlockPos anchor) {
		this.blocks = blocks;
		this.bounds = bounds;
		this.anchor = anchor;
	}
	
	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}
	
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 15 << 20 | lightValue << 4;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);
		if (bounds.contains(pos) && blocks.containsKey(pos)) {
			return blocks.get(pos);
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return Biomes.VOID;
	}

	@Override
	public int getStrongPower(BlockPos pos, Direction direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.FLAT;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
		if (bounds.contains(anchor.subtract(pos)))
			return getBlockState(pos).isSideSolid(this, pos, side);
		else 
			return _default;
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

}
