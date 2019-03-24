package com.simibubi.mightyarchitect.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockDesignAnchor extends BlockForMightyArchitects {

	public static final PropertyBool compass = PropertyBool.create("compass");
	
	public BlockDesignAnchor(String name) {
		super(name, Material.ROCK);
		this.setHardness(2.0f);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return getDefaultState().withProperty(compass, true);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {compass});
    }
}
