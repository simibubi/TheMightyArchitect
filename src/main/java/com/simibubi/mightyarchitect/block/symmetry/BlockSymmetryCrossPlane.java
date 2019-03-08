package com.simibubi.mightyarchitect.block.symmetry;

import com.simibubi.mightyarchitect.symmetry.SymmetryCrossPlane;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSymmetryCrossPlane extends BlockSymmetry {

	public static final PropertyEnum<SymmetryCrossPlane.Align> align = PropertyEnum.create("align", SymmetryCrossPlane.Align.class);
	
	public BlockSymmetryCrossPlane(String name) {
		super(name);
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(align, SymmetryCrossPlane.Align.Y));	
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, align);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((SymmetryCrossPlane.Align) state.getProperties().get(align)).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= SymmetryCrossPlane.Align.values().length) 
			return this.getDefaultState();
		return this.getDefaultState().withProperty(align, SymmetryCrossPlane.Align.values()[meta]);
	}
	
}
