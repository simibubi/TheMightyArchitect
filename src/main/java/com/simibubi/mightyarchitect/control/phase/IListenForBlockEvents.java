package com.simibubi.mightyarchitect.control.phase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface IListenForBlockEvents {

	public void onBlockPlaced(BlockPos pos, IBlockState state);
	public void onBlockBroken(BlockPos pos);
	
}
