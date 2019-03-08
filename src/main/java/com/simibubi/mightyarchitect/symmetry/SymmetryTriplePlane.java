package com.simibubi.mightyarchitect.symmetry;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryTriplePlane;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SymmetryTriplePlane extends SymmetryElement {

	public static enum Align implements IStringSerializable {
		X("x"),
		Y("y"),
		Z("z");
		
		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getName() { return name; }
		@Override public String toString() { return name; }
	}

	public SymmetryTriplePlane(Vec3d pos) {
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
	public Map<BlockPos, IBlockState> process(BlockPos position, IBlockState block) {
		Map<BlockPos, IBlockState> result = new HashMap<>();

		switch ((Align) orientation) {
		case X:
			break;
		case Y:
			result.put(flipX(position), flipX(block));
			result.put(flipZ(position), flipZ(block));
			result.put(flipX(flipZ(position)), flipX(flipZ(block)));

			result.put(flipD1(position), flipD1(block));
			result.put(flipD1(flipX(position)), flipD1(flipX(block)));
			result.put(flipD1(flipZ(position)), flipD1(flipZ(block)));
			result.put(flipD1(flipX(flipZ(position))), flipD1(flipX(flipZ(block))));
			
			break;
		case Z:
			break;
		default:
			break;
		
		}
		
		return result;
	}

	@Override
	public String typeName() {
		return TRIPLE_PLANE;
	}

	@Override
	public IBlockState getModel() {
		return AllBlocks.symmetry_triple_plane.getDefaultState().withProperty(BlockSymmetryTriplePlane.align, (Align) orientation);
	}

}
