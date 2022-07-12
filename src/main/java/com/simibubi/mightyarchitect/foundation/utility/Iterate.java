package com.simibubi.mightyarchitect.foundation.utility;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class Iterate {

	public static final boolean[] trueAndFalse = { true, false };
	public static final int[] zeroAndOne = { 1, -1 };
	public static final int[] positiveAndNegative = { 1, -1 };
	public static final Direction[] directions = Direction.values();
	public static final Direction[] horizontalDirections = getHorizontals();
	public static final Axis[] axes = Axis.values();

	private static Direction[] getHorizontals() {
		Direction[] directions = new Direction[4];
		for (int i = 0; i < 4; i++)
			directions[i] = Direction.from2DDataValue(i);
		return directions;
	}
	
	public static List<BlockPos> hereAndBelow(BlockPos pos) {
		return Arrays.asList(pos, pos.below());
	}
	                                                    
	
}
