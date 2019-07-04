package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Rotation;

public enum BlockOrientation {

	NONE(null, null),

	TOP(Half.TOP, null), BOTTOM(Half.BOTTOM, null),

	UP(null, Direction.UP), DOWN(null, Direction.DOWN), NORTH(null, Direction.NORTH), SOUTH(null, Direction.SOUTH),
	EAST(null, Direction.EAST), WEST(null, Direction.WEST),

	TOP_UP(Half.TOP, Direction.UP), TOP_DOWN(Half.TOP, Direction.DOWN), TOP_NORTH(Half.TOP, Direction.NORTH),
	TOP_SOUTH(Half.TOP, Direction.SOUTH), TOP_EAST(Half.TOP, Direction.EAST), TOP_WEST(Half.TOP, Direction.WEST),

	BOTTOM_UP(Half.BOTTOM, Direction.UP), BOTTOM_DOWN(Half.BOTTOM, Direction.DOWN),
	BOTTOM_NORTH(Half.BOTTOM, Direction.NORTH), BOTTOM_SOUTH(Half.BOTTOM, Direction.SOUTH),
	BOTTOM_EAST(Half.BOTTOM, Direction.EAST), BOTTOM_WEST(Half.BOTTOM, Direction.WEST);

	private Half half;
	private Direction facing;

	private BlockOrientation(Half half, Direction facing) {
		this.half = half;
		this.facing = facing;
	}

	public static BlockOrientation valueOf(Half half, Direction facing) {
		for (BlockOrientation e : values()) {
			if (e.half == half && e.facing == facing)
				return e;
		}
		return NONE;
	}

	public BlockOrientation withRotation(int angle) {
		if (!hasFacing())
			return this;

		if (facing.getHorizontalIndex() == -1)
			return this;

		Direction facing2 = facing;

		for (; angle > -360; angle -= 90) {
			facing2 = facing2.rotateY();
		}

		return valueOf(half, facing2);
	}

	public BlockOrientation withMirror(Axis axis) {
		if (!hasFacing() || facing.getAxis() != axis)
			return this;

		return valueOf(half, facing.getOpposite());
	}

	public static BlockOrientation byState(BlockState state) {
		Half half = null;
		Direction facing = null;

		if (state.has(BlockStateProperties.HALF))
			half = state.get(BlockStateProperties.HALF);

		if (state.has(BlockStateProperties.FACING))
			facing = state.get(BlockStateProperties.FACING);

		if (state.has(BlockStateProperties.FACING_EXCEPT_UP))
			facing = state.get(BlockStateProperties.FACING);

		if (state.has(BlockStateProperties.HORIZONTAL_FACING))
			facing = state.get(BlockStateProperties.FACING);

		if (state.getBlock() instanceof TrapDoorBlock)
			facing = facing.getOpposite();

		if (state.has(BlockStateProperties.AXIS))
			facing = Direction.getFacingFromAxis(AxisDirection.POSITIVE, state.get(BlockStateProperties.AXIS));

		if (state.has(BlockStateProperties.HORIZONTAL_AXIS))
			facing = Direction.getFacingFromAxis(AxisDirection.POSITIVE, state.get(BlockStateProperties.AXIS));

		return valueOf(half, facing);
	}

	public static BlockOrientation valueOf(char character) {
		return values()[character - 'A'];
	}

	public char asChar() {
		return (char) ('A' + ordinal());
	}

	public BlockState apply(BlockState state, boolean forceAxis) {
		BlockState newState = state;
		newState = newState.rotate(getRotation());

		if (hasHalf() && state.has(BlockStateProperties.HALF))
			newState = newState.with(BlockStateProperties.HALF, half);

		if (hasFacing() && state.has(BlockStateProperties.FACING))
			if (facing.getAxis().isHorizontal() && state.get(BlockStateProperties.FACING).getAxis().isHorizontal())
				newState = newState.with(BlockStateProperties.FACING, facing);

		if (hasFacing() && state.has(BlockStateProperties.FACING_EXCEPT_UP))
			if (facing.getAxis().isHorizontal()
					&& state.get(BlockStateProperties.FACING_EXCEPT_UP).getAxis().isHorizontal())
				newState = newState.with(BlockStateProperties.FACING_EXCEPT_UP, facing);

		if (hasFacing() && state.has(BlockStateProperties.HORIZONTAL_FACING))
			if (facing.getAxis().isHorizontal())
				newState = newState.with(BlockStateProperties.HORIZONTAL_FACING, facing);

		if (state.getBlock() instanceof TrapDoorBlock)
			state = state.with(BlockStateProperties.HORIZONTAL_FACING,
					state.get(BlockStateProperties.HORIZONTAL_FACING).getOpposite());

		if (hasFacing() && state.has(BlockStateProperties.AXIS)) {
			Axis axis = state.get(BlockStateProperties.AXIS);
			if (axis == Axis.Y && forceAxis)
				newState = newState.with(BlockStateProperties.AXIS, facing.getAxis());
		}

		if (hasFacing() && state.has(BlockStateProperties.HORIZONTAL_AXIS)) {
			Axis axis = state.get(BlockStateProperties.HORIZONTAL_AXIS);
			if (axis == Axis.Y && forceAxis)
				newState = newState.with(BlockStateProperties.HORIZONTAL_AXIS, facing.getAxis());
		}

		return newState;
	}

	private Rotation getRotation() {
		if (!hasFacing())
			return Rotation.NONE;

		switch (facing) {
		case EAST:
			return Rotation.COUNTERCLOCKWISE_90;
		case NORTH:
			return Rotation.CLOCKWISE_180;
		case WEST:
			return Rotation.CLOCKWISE_90;
		default:
			return Rotation.NONE;
		}
	}

	public boolean hasFacing() {
		return facing != null;
	}

	public boolean hasHalf() {
		return half != null;
	}

	public Direction getFacing() {
		return facing;
	}

	public Half getHalf() {
		return half;
	}

	@Override
	public String toString() {
		return "Orientation: " + (hasHalf() ? half.getName() + " " : "Solid ") + (hasFacing() ? facing.getName() : "");
	}

	public BlockState apply(BlockState iBlockState) {
		return apply(iBlockState, false);
	}

}
