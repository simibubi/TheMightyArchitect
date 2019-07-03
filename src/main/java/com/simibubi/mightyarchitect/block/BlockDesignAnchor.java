package com.simibubi.mightyarchitect.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;

public class BlockDesignAnchor extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");
	
	public BlockDesignAnchor() {
		super(Properties.create(Material.ROCK));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(compass);
		super.fillStateContainer(builder);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(compass, true);
	}
}
