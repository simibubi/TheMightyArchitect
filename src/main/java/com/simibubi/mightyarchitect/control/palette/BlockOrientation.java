package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

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

		if (facing.get2DDataValue() == -1)
			return this;

		Direction facing2 = facing;

		for (; angle > -360; angle -= 90) {
			facing2 = facing2.getClockWise();
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

		if (state.hasProperty(BlockStateProperties.HALF))
			half = state.getValue(BlockStateProperties.HALF);

		if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE)
			half = state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM ? Half.BOTTOM : Half.TOP;

		if (state.hasProperty(BlockStateProperties.FACING))
			facing = state.getValue(BlockStateProperties.FACING);

		if (state.hasProperty(BlockStateProperties.FACING_HOPPER))
			facing = state.getValue(BlockStateProperties.FACING_HOPPER);

		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

		if (state.getBlock() instanceof TrapDoorBlock)
			facing = facing.getOpposite();

		if (state.hasProperty(BlockStateProperties.AXIS))
			facing = Direction.get(AxisDirection.POSITIVE, state.getValue(BlockStateProperties.AXIS));

		if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			facing = Direction.get(AxisDirection.POSITIVE,
					state.getValue(BlockStateProperties.HORIZONTAL_AXIS));

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
		newState = newState.rotate(Minecraft.getInstance().level, BlockPos.ZERO, getRotation());

		if (hasHalf() && state.hasProperty(BlockStateProperties.HALF))
			newState = newState.setValue(BlockStateProperties.HALF, half);

		if (hasHalf() && state.hasProperty(SlabBlock.TYPE))
			if (state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE)
				newState = newState.setValue(SlabBlock.TYPE, half == Half.TOP ? SlabType.TOP : SlabType.BOTTOM);

		if (hasFacing() && state.hasProperty(BlockStateProperties.FACING))
			if (state.getValue(BlockStateProperties.FACING).getAxis().isVertical() && forceAxis)
				newState = newState.setValue(BlockStateProperties.FACING, facing);

		if (hasFacing() && state.hasProperty(BlockStateProperties.FACING_HOPPER))
			if (state.getValue(BlockStateProperties.FACING_HOPPER).getAxis().isVertical() && forceAxis)
				newState = newState.setValue(BlockStateProperties.FACING_HOPPER, facing);

//		if (forceAxis && newState.getBlock() instanceof TrapDoorBlock)
//			newState = newState.rotate(Rotation.CLOCKWISE_180);

		if (hasFacing() && state.hasProperty(BlockStateProperties.AXIS)) {
			Axis axis = state.getValue(BlockStateProperties.AXIS);
			if (axis == Axis.Y && forceAxis)
				newState = newState.setValue(BlockStateProperties.AXIS, facing.getAxis());
		}

		if (hasFacing() && state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
			Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			if (axis == Axis.Y && forceAxis)
				newState = newState.setValue(BlockStateProperties.HORIZONTAL_AXIS, facing.getAxis());
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
		return "Orientation: " + (hasHalf() ? half.getSerializedName() + " " : "Solid ") + (hasFacing() ? facing.getSerializedName() : "");
	}

	public BlockState apply(BlockState iBlockState) {
		return apply(iBlockState, false);
	}

}
