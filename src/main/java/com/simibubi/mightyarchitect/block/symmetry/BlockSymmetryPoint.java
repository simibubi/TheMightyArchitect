package com.simibubi.mightyarchitect.block.symmetry;

import com.simibubi.mightyarchitect.symmetry.SymmetryPoint;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSymmetryPoint extends BlockSymmetry {

	public static final PropertyEnum<SymmetryPoint.Align> align = PropertyEnum.create("align", SymmetryPoint.Align.class);
	
	public BlockSymmetryPoint(String name) {
		super(name);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(align, SymmetryPoint.Align.Point));	
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, align);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((SymmetryPoint.Align) state.getProperties().get(align)).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= SymmetryPoint.Align.values().length) 
			return this.getDefaultState();
		return this.getDefaultState().withProperty(align, SymmetryPoint.Align.values()[meta]);
	}
	
}
