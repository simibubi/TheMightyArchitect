package com.simibubi.mightyarchitect.buildomatico.model.schematic;

import java.util.Map;

import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class SchematicBlockAccess implements IBlockAccess {

	private Map<BlockPos, IBlockState> blocks;
	private Cuboid bounds;
	private BlockPos anchor;
	
	public SchematicBlockAccess(Map<BlockPos, IBlockState> blocks, Cuboid bounds, BlockPos anchor) {
		this.blocks = blocks;
		this.bounds = bounds;
		this.anchor = anchor;
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
	public IBlockState getBlockState(BlockPos globalPos) {
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
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.FLAT;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		if (bounds.contains(anchor.subtract(pos)))
			return getBlockState(pos).isSideSolid(this, pos, side);
		else 
			return _default;
	}

}
