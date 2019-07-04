package com.simibubi.mightyarchitect.control;

import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.compose.Cuboid;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class TemplateBlockAccess implements IEnviromentBlockReader {

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
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);
		if (bounds.contains(pos) && blocks.containsKey(pos)) {
			return blocks.get(pos);
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	@Override
	public IFluidState getFluidState(BlockPos pos) {
		return null;
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return Biomes.THE_VOID;
	}

	@Override
	public int getLightFor(LightType type, BlockPos pos) {
		return 10;
	}

}
