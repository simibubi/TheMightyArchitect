package com.simibubi.mightyarchitect.block.symmetry;

import com.simibubi.mightyarchitect.symmetry.SymmetryAxis;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSymmetryAxis extends BlockSymmetry {

	public static final PropertyEnum<SymmetryAxis.Align> align = PropertyEnum.create("align", SymmetryAxis.Align.class);
	
	public BlockSymmetryAxis(String name) {
		super(name);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(align, SymmetryAxis.Align.Y));	
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, align);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((SymmetryAxis.Align) state.getProperties().get(align)).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= SymmetryAxis.Align.values().length) 
			return this.getDefaultState();
		return this.getDefaultState().withProperty(align, SymmetryAxis.Align.values()[meta]);
	}
	
}
