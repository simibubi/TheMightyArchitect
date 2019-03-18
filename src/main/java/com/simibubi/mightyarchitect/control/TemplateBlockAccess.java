package com.simibubi.mightyarchitect.control;

import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.compose.Cuboid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class TemplateBlockAccess implements IBlockAccess {

	private Map<BlockPos, IBlockState> blocks;
	private Cuboid bounds;
	private BlockPos anchor;
	
	public TemplateBlockAccess(Map<BlockPos, IBlockState> blocks, Cuboid bounds, BlockPos anchor) {
		this.blocks = blocks;
		this.bounds = bounds;
		this.anchor = anchor;
	}
	
	public void writeToTemplate(Template template) {
		blocks.forEach((pos, state) -> template.putBlock(pos, state));
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

	public Map<BlockPos, IBlockState> getBlockMap() {
		return blocks;
	}

}
