package com.simibubi.mightyarchitect.symmetry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SymmetryEmptySlot extends SymmetryElement {

	public static enum Align implements IStringSerializable {
		None("none");
		
		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getName() { return name; }
		@Override public String toString() { return name; }
	}
	
	public SymmetryEmptySlot(Vec3d pos) {
		super(pos);
		orientation = Align.None;
	}

	@Override
	protected void setOrientation() {
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
		return EMPTY;
	}

	@Override
	public IBlockState getModel() {
		return null;
	}

}
