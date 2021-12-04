package com.simibubi.mightyarchitect.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;

import net.minecraft.block.AbstractBlock.Properties;

public class DesignAnchorBlock extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");
	
	public DesignAnchorBlock() {
		super(Properties.of(Material.STONE));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(compass);
		super.createBlockStateDefinition(builder);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return defaultBlockState().setValue(compass, true);
	}
}
