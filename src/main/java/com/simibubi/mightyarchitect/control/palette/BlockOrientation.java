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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;

public enum BlockOrientation {

	NONE(null, null),

	TOP(EnumBlockHalf.TOP, null), BOTTOM(EnumBlockHalf.BOTTOM, null),

	UP(null, EnumFacing.UP), DOWN(null, EnumFacing.DOWN), NORTH(null, EnumFacing.NORTH), SOUTH(null,
			EnumFacing.SOUTH), EAST(null, EnumFacing.EAST), WEST(null, EnumFacing.WEST),

	TOP_UP(EnumBlockHalf.TOP, EnumFacing.UP), TOP_DOWN(EnumBlockHalf.TOP, EnumFacing.DOWN), TOP_NORTH(EnumBlockHalf.TOP,
			EnumFacing.NORTH), TOP_SOUTH(EnumBlockHalf.TOP, EnumFacing.SOUTH), TOP_EAST(EnumBlockHalf.TOP,
					EnumFacing.EAST), TOP_WEST(EnumBlockHalf.TOP, EnumFacing.WEST),

	BOTTOM_UP(EnumBlockHalf.BOTTOM, EnumFacing.UP), BOTTOM_DOWN(EnumBlockHalf.BOTTOM, EnumFacing.DOWN), BOTTOM_NORTH(
			EnumBlockHalf.BOTTOM, EnumFacing.NORTH), BOTTOM_SOUTH(EnumBlockHalf.BOTTOM, EnumFacing.SOUTH), BOTTOM_EAST(
					EnumBlockHalf.BOTTOM, EnumFacing.EAST), BOTTOM_WEST(EnumBlockHalf.BOTTOM, EnumFacing.WEST);

	private EnumBlockHalf half;
	private EnumFacing facing;

	private BlockOrientation(EnumBlockHalf half, EnumFacing facing) {
		this.half = half;
		this.facing = facing;
	}

	public static BlockOrientation valueOf(EnumBlockHalf half, EnumFacing facing) {
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

		EnumFacing facing2 = facing;

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

	public static BlockOrientation byState(IBlockState state) {
		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();

		EnumBlockHalf half = null;
		EnumFacing facing = null;

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
				facing = axis == Axis.X ? EnumFacing.EAST : axis == Axis.Z ? EnumFacing.SOUTH : facing;
			}

			if (property == BlockLog.LOG_AXIS) {
				EnumAxis axis = ((EnumAxis) properties.get(BlockLog.LOG_AXIS));
				facing = axis == EnumAxis.X ? EnumFacing.EAST
						: axis == EnumAxis.Y ? EnumFacing.UP : axis == EnumAxis.Z ? EnumFacing.SOUTH : facing;
			}

			if (property instanceof PropertyDirection) {
				facing = (EnumFacing) properties.get(property);
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

	public IBlockState apply(IBlockState state, boolean forceAxis) {
		IBlockState newState = state;
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
						&& ((EnumFacing) properties.get((PropertyDirection) property)).getHorizontalIndex() == -1)
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
					newState = newState.withProperty(BlockLog.LOG_AXIS, facing.getAxis() == Axis.X ? EnumAxis.X : facing.getAxis() == Axis.Z ? EnumAxis.Z : axis);
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

	public EnumFacing getFacing() {
		return facing;
	}

	public EnumBlockHalf getHalf() {
		return half;
	}

	@Override
	public String toString() {
		return "Orientation: " + (hasHalf() ? half.getName() + " " : "Solid ") + (hasFacing() ? facing.getName() : "");
	}

	public IBlockState apply(IBlockState iBlockState) {
		return apply(iBlockState, false);
	}

}
