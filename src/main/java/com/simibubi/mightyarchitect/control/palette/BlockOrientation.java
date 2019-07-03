package com.simibubi.mightyarchitect.control.palette;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTrapDoor.DoorHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;

public enum BlockOrientation {

	NONE(null, null),

	TOP(EnumBlockHalf.TOP, null), BOTTOM(EnumBlockHalf.BOTTOM, null),

	UP(null, Direction.UP), DOWN(null, Direction.DOWN), NORTH(null, Direction.NORTH), SOUTH(null,
			Direction.SOUTH), EAST(null, Direction.EAST), WEST(null, Direction.WEST),

	TOP_UP(EnumBlockHalf.TOP, Direction.UP), TOP_DOWN(EnumBlockHalf.TOP, Direction.DOWN), TOP_NORTH(EnumBlockHalf.TOP,
			Direction.NORTH), TOP_SOUTH(EnumBlockHalf.TOP, Direction.SOUTH), TOP_EAST(EnumBlockHalf.TOP,
					Direction.EAST), TOP_WEST(EnumBlockHalf.TOP, Direction.WEST),

	BOTTOM_UP(EnumBlockHalf.BOTTOM, Direction.UP), BOTTOM_DOWN(EnumBlockHalf.BOTTOM, Direction.DOWN), BOTTOM_NORTH(
			EnumBlockHalf.BOTTOM, Direction.NORTH), BOTTOM_SOUTH(EnumBlockHalf.BOTTOM, Direction.SOUTH), BOTTOM_EAST(
					EnumBlockHalf.BOTTOM, Direction.EAST), BOTTOM_WEST(EnumBlockHalf.BOTTOM, Direction.WEST);

	private EnumBlockHalf half;
	private Direction facing;

	private BlockOrientation(EnumBlockHalf half, Direction facing) {
		this.half = half;
		this.facing = facing;
	}

	public static BlockOrientation valueOf(EnumBlockHalf half, Direction facing) {
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
		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();

		EnumBlockHalf half = null;
		Direction facing = null;

		// This sucks
		for (IProperty<?> property : properties.keySet()) {
			if (property == BlockSlab.HALF)
				half = (EnumBlockHalf) properties.get(BlockSlab.HALF);

			if (property == BlockStairs.HALF)
				half = ((EnumHalf) properties.get(BlockStairs.HALF)) == EnumHalf.TOP ? EnumBlockHalf.TOP
						: EnumBlockHalf.BOTTOM;

			if (property == BlockTrapDoor.HALF)
				half = ((DoorHalf) properties.get(BlockTrapDoor.HALF)) == DoorHalf.TOP ? EnumBlockHalf.TOP
						: EnumBlockHalf.BOTTOM;

			if (property == BlockRotatedPillar.AXIS) {
				Axis axis = ((Axis) properties.get(BlockRotatedPillar.AXIS));
				facing = axis == Axis.X ? Direction.EAST : axis == Axis.Z ? Direction.SOUTH : facing;
			}

			if (property == BlockLog.LOG_AXIS) {
				EnumAxis axis = ((EnumAxis) properties.get(BlockLog.LOG_AXIS));
				facing = axis == EnumAxis.X ? Direction.EAST
						: axis == EnumAxis.Y ? Direction.UP : axis == EnumAxis.Z ? Direction.SOUTH : facing;
			}

			if (property instanceof PropertyDirection) {
				facing = (Direction) properties.get(property);

				// Trap doors decided to be the wrong way around
				if (state.getBlock() instanceof BlockTrapDoor)
					facing = facing.getOpposite();
			}
		}

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
		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
		newState = newState.withRotation(getRotation());

		for (IProperty<?> property : properties.keySet()) {
			if (hasHalf() && property == BlockSlab.HALF)
				newState = newState.withProperty(BlockSlab.HALF, half);

			if (hasHalf() && property == BlockStairs.HALF)
				newState = newState.withProperty(BlockStairs.HALF,
						half == EnumBlockHalf.TOP ? EnumHalf.TOP : EnumHalf.BOTTOM);

			if (hasHalf() && property == BlockTrapDoor.HALF)
				newState = newState.withProperty(BlockTrapDoor.HALF,
						half == EnumBlockHalf.TOP ? DoorHalf.TOP : DoorHalf.BOTTOM);

			if (hasFacing() && property instanceof PropertyDirection) {
				if (facing.getAxis() != Axis.Y
						&& ((Direction) properties.get((PropertyDirection) property)).getHorizontalIndex() == -1)
					if (newState.getBlock() instanceof BlockTrapDoor)
						newState = newState.withProperty(((PropertyDirection) property), facing.getOpposite());
					else
						newState = newState.withProperty(((PropertyDirection) property), facing);
			}

			if (hasFacing() && property == BlockRotatedPillar.AXIS) {
				Axis axis = ((Axis) properties.get(BlockRotatedPillar.AXIS));
				if (axis == Axis.Y && forceAxis)
					newState = newState.withProperty(BlockRotatedPillar.AXIS, facing.getAxis());
			}

			if (hasFacing() && property == BlockLog.LOG_AXIS) {
				EnumAxis axis = ((EnumAxis) properties.get(BlockLog.LOG_AXIS));
				if (axis == EnumAxis.Y && forceAxis)
					newState = newState.withProperty(BlockLog.LOG_AXIS,
							facing.getAxis() == Axis.X ? EnumAxis.X : facing.getAxis() == Axis.Z ? EnumAxis.Z : axis);
			}
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

	public EnumBlockHalf getHalf() {
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
