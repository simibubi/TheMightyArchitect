package com.simibubi.mightyarchitect.symmetry;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryPoint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SymmetryPoint extends SymmetryElement {

	public static enum Align implements IStringSerializable {
		Point("point");
		
		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getName() { return name; }
		@Override public String toString() { return name; }
	}
	
	public SymmetryPoint(Vec3d pos) {
		super(pos);		
		orientation = Align.Point;
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
	public Map<BlockPos, IBlockState> process(BlockPos position, IBlockState block) {
		return new HashMap<>();
	}

	@Override
	public String typeName() {
		return POINT;
	}

	@Override
	public IBlockState getModel() {
		return AllBlocks.symmetry_point.getDefaultState().withProperty(BlockSymmetryPoint.align, (Align) orientation);
	}
	
	
	

}
