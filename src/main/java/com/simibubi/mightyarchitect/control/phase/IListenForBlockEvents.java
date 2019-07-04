package com.simibubi.mightyarchitect.control.phase;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface IListenForBlockEvents {

	public void onBlockPlaced(BlockPos pos, BlockState state);
	public void onBlockBroken(BlockPos pos);
	
}
