package com.simibubi.mightyarchitect.block.symmetry;

import com.simibubi.mightyarchitect.symmetry.SymmetryTriplePlane;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSymmetryTriplePlane extends BlockSymmetry {

public static final PropertyEnum<SymmetryTriplePlane.Align> align = PropertyEnum.create("align", SymmetryTriplePlane.Align.class);
	
	public BlockSymmetryTriplePlane(String name) {
		super(name);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(align, SymmetryTriplePlane.Align.Y));	
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, align);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((SymmetryTriplePlane.Align) state.getProperties().get(align)).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= SymmetryTriplePlane.Align.values().length) 
			return this.getDefaultState();
		return this.getDefaultState().withProperty(align, SymmetryTriplePlane.Align.values()[meta]);
	}
	
}
