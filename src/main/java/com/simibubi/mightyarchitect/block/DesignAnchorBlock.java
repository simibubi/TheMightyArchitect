package com.simibubi.mightyarchitect.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(compass, true);
	}
}
