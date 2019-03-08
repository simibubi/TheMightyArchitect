package com.simibubi.mightyarchitect.block.symmetry;

import com.simibubi.mightyarchitect.symmetry.SymmetryPlane;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSymmetryPlane extends BlockSymmetry {

	public static final PropertyEnum<SymmetryPlane.Align> align = PropertyEnum.create("align", SymmetryPlane.Align.class);
	
	public BlockSymmetryPlane(String name) {
		super(name);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(align, SymmetryPlane.Align.XY));	
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, align);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((SymmetryPlane.Align) state.getProperties().get(align)).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= SymmetryPlane.Align.values().length) 
			return this.getDefaultState();
		return this.getDefaultState().withProperty(align, SymmetryPlane.Align.values()[meta]);
	}
	
}
