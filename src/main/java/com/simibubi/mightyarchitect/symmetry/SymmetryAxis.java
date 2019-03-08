package com.simibubi.mightyarchitect.symmetry;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryAxis;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SymmetryAxis extends SymmetryElement {

	public static enum Align implements IStringSerializable {
		X("x"),
		Y("y"),
		Z("z");
		
		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getName() { return name; }
		@Override public String toString() { return name; }
	}

	public SymmetryAxis(Vec3d pos) {
		super(pos);
		orientation = Align.X;
	}

	@Override
	protected void setOrientation() {
		if (orientationIndex < 0) orientationIndex += Align.values().length;
		if (orientationIndex >= Align.values().length) orientationIndex -= Align.values().length;
		orientation = Align.values()[orientationIndex];
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public String typeName() {
		return AXIS;
	}

	@Override
	public IBlockState getModel() {
		return AllBlocks.symmetry_axis.getDefaultState().withProperty(BlockSymmetryAxis.align, (Align) orientation);
	}

	@Override
	public Map<BlockPos, IBlockState> process(BlockPos position, IBlockState block) {
		return new HashMap<>();
	}
	
	

}
