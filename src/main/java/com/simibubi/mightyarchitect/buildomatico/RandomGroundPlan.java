package com.simibubi.mightyarchitect.buildomatico;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class RandomGroundPlan {

	public static final int MAX_MAIN_CUBOIDS = 5;
	public static final int MAX_CUBOIDS = 10;
	public static final int MAX_MAIN_LENGTH = 15;
	public static final int MAX_LENGTH = 25;
	public static final int MIN_LENGTH = 5;
	public static final int MAX_FOUNDATION_HEIGHT = 4;
	public static final int MIN_FOUNDATION_HEIGHT = 3;
	public static final int MIN_FLOOR_HEIGHT = 3;
	public static final int MAX_FLOOR_HEIGHT = 8;
	public static final int MAX_FLOORS = 4;

	private final static BlockPos origin = new BlockPos(0, 0, 0);

	private static GroundPlan generate() {
		final int crossed = 0, attached = 1, stacked = 2;
		GroundPlan compound = new GroundPlan();
		Random dice = new Random();

		// Ground Plan
		switch (dice.nextInt(3)) {
		case crossed:
			generateCrossed(compound);
			break;
		case attached:
			generateAttached(compound);
			break;
		case stacked:
			generateStacked(compound);
			break;
		default:
			break;
		}

		// Stacking
		List<Cuboid> foundation = compound.getCuboidsOnLayer(0);
		List<Cuboid> groundFloor = compound.getCuboidsOnLayer(1);

		boolean outlier = foundation.size() > 2 && maybe();
		int outlierIndex = random(foundation.size());
		int floorHeight = random(MIN_FLOOR_HEIGHT, MAX_FLOOR_HEIGHT);

		for (Cuboid cuboid : foundation) {
			Cuboid clone = cuboid.stack();
			clone.height = floorHeight;
			compound.add(clone, 1);
		}

		if (outlier) {
			foundation.get(outlierIndex).height = random(MIN_FOUNDATION_HEIGHT, MAX_FOUNDATION_HEIGHT + 3);
			foundation.get(outlierIndex).detach();
			groundFloor.get(outlierIndex).y = foundation.get(outlierIndex).height;
			groundFloor.get(outlierIndex).height = random(floorHeight, MAX_FLOOR_HEIGHT + 3);
			groundFloor.get(outlierIndex).detach();
		}

		final int allSame = 0, firstHigher = 1, random = 2;

		switch (dice.nextInt(3)) {
		case allSame:
			stackAllSame(compound);
			break;
		case firstHigher:
			stackFirstHigher(compound);
			break;
		case random:
			stackRandom(compound);
			break;
		default:
			break;
		}

		// Detailing

		return compound;
	}

	private static void stackRandom(GroundPlan compound) {

		for (int floor = 2; floor <= MAX_FLOORS; floor++) {
			for (Cuboid cuboid : compound.getCuboidsOnLayer(floor - 1)) {
				if (maybe()) {
					compound.add(cuboid.stack(), floor);
				}
			}
		}

	}

	private static void stackFirstHigher(GroundPlan compound) {
		int amountNextLayer = compound.getCuboidsOnLayer(1).size();

		for (int floor = 2; floor <= MAX_FLOORS; floor++) {
			amountNextLayer -= random(amountNextLayer + 1);

			for (int cuboidIndex = 0; cuboidIndex < amountNextLayer; cuboidIndex++) {
				Cuboid cuboid = compound.getCuboidsOnLayer(floor - 1).get(cuboidIndex);
				compound.add(cuboid.stack(), floor);
			}
		}

	}

	private static void stackAllSame(GroundPlan compound) {
		int floors = random(1, MAX_FLOORS);

		for (int floor = 2; floor <= floors; floor++) {
			for (Cuboid cuboid : compound.getCuboidsOnLayer(floor - 1)) {
				compound.add(cuboid.stack(), floor);
			}
		}

	}

	private static void generateCrossed(GroundPlan compound) {

		int widthFirst = randomOdd(MIN_LENGTH, MAX_LENGTH / 2);
		int widthSecond = maybe() ? widthFirst : randomOdd(MIN_LENGTH, MAX_LENGTH / 2);
		int lengthFirst = randomOdd(widthSecond + 6, MAX_LENGTH);
		int lengthSecond = randomOdd(widthFirst + 6, MAX_LENGTH);
		int height = random(MIN_FOUNDATION_HEIGHT, MAX_FOUNDATION_HEIGHT);

		int firstMaxNudge = (lengthFirst - widthSecond - 4) / 2;
		int secondMaxNudge = (lengthSecond - widthFirst - 4) / 2;

		Cuboid first = new Cuboid(origin, widthFirst, height, lengthFirst);
		Cuboid second = new Cuboid(origin, lengthSecond, height, widthSecond);

		first.centerHorizontallyOn(origin);
		second.centerHorizontallyOn(origin);

		if (random(3) != 0) {
			first.move(0, 0, random(-firstMaxNudge, firstMaxNudge));
			second.move(random(-secondMaxNudge, secondMaxNudge), 0, 0);
		}

		compound.add(first, 0);
		compound.add(second, 0);

	}

	private static void generateAttached(GroundPlan compound) {

		int cuboidCount = random(MAX_MAIN_CUBOIDS);

		int primaryWidth = randomOdd(MIN_LENGTH, MAX_MAIN_LENGTH);
		int primaryLength = randomOdd(MIN_LENGTH, MAX_LENGTH);
		int height = random(MIN_FOUNDATION_HEIGHT, MAX_FOUNDATION_HEIGHT);

		Cuboid primary = new Cuboid(origin, primaryWidth, height, primaryLength);
		primary.centerHorizontallyOn(origin);
		compound.add(primary, 0);
		
		List<Cuboid> existing = new LinkedList<>();
		existing.add(primary);

		List<AxisDirection> directions = new LinkedList<>();
		directions.add(AxisDirection.NEGATIVE);
		directions.add(AxisDirection.POSITIVE);

		// Attach things
		while (cuboidCount-- > 0) {
			Collections.shuffle(existing);
			Cuboid attached = null;
			boolean found = false;

			for (Cuboid attachTo : existing) {
				if (found) break;
				Collections.shuffle(directions);

				for (AxisDirection direction : directions) {
					int width = attachTo.width;
					int length = attachTo.length;
					for (int tries = 0; width == attachTo.width && tries < 1000; tries++)
						width = randomOdd(MIN_LENGTH, MAX_LENGTH / 2);
					for (int tries = 0; length == attachTo.length && tries < 1000; tries++)
						length = randomOdd(MIN_LENGTH, MAX_LENGTH / 2);

					attached = new Cuboid(origin, width, height, length);
					Axis side = attachTo.getOrientation() == Axis.X ? Axis.Z : Axis.X;
					int maxNudge = side == Axis.X ? (Math.abs(attachTo.length - attached.length) - 4) / 2
							: (Math.abs(attachTo.width - attached.width) - 4) / 2;
					int shift = random(-maxNudge, maxNudge);
					EnumFacing facingFromAxis = EnumFacing.getFacingFromAxis(direction, side);

					if (compound.canCuboidAttachTo(attached, attachTo, facingFromAxis, shift)) {
						attached.attachTo(attachTo, facingFromAxis, shift);
						found = true;
						break;
					}
				}
			}
			
			if (found) {
				existing.add(attached);
				compound.add(attached, 0);
			}
		}

	}

	private static void generateStacked(GroundPlan compound) {

		int primaryWidth = randomOdd(MIN_LENGTH, MAX_MAIN_LENGTH);
		int primaryLength = randomOdd(MIN_LENGTH, MAX_LENGTH);
		int height = random(MIN_FOUNDATION_HEIGHT, MAX_FOUNDATION_HEIGHT);

		Cuboid primary = new Cuboid(origin, primaryWidth, height, primaryLength);
		primary.centerHorizontallyOn(origin);

		int secondaryWidth = primaryWidth;
		int secondaryLength = primaryLength;

		for (int moreTries = 0; secondaryWidth == primaryWidth && moreTries < 1000; moreTries++)
			secondaryWidth = randomOdd(MIN_LENGTH, MAX_MAIN_LENGTH);
		for (int moreTries = 0; secondaryLength == primaryLength && moreTries < 1000; moreTries++)
			secondaryLength = randomOdd(MIN_LENGTH, MAX_MAIN_LENGTH);

		Cuboid secondary = new Cuboid(origin, secondaryWidth, height, secondaryLength);
		Cuboid secondaryClone = secondary.clone();

		Axis axis = primary.getOrientation() == Axis.X ? Axis.Z : Axis.X;
		int maxNudge = axis == Axis.X ? (Math.abs(primary.length - secondary.length) - 4) / 2
				: (Math.abs(primary.width - secondary.width) - 4) / 2;

		secondary.attachTo(primary, EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis), random(maxNudge));
		secondaryClone.attachTo(primary, EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis), random(maxNudge));

		compound.add(primary, 0);
		compound.add(secondary, 0);
		compound.add(secondaryClone, 0);

	}

	private static int random(int upper) {
		return random(0, upper);
	}

	private static int random(int lower, int upper) {
		if (upper <= lower)
			return lower;
		Random dice = new Random();
		return dice.nextInt(upper - lower) + lower;
	}

	/** lower has to be odd */
	private static int randomOdd(int lower, int upper) {
		return random(upper - lower) / 2 * 2 + lower;
	}

	private static boolean maybe() {
		return new Random().nextBoolean();
	}

	public GroundPlan planCuboids(Context context) {
		GroundPlan randomGroundPlan = generate();
		randomGroundPlan.setContext(context);
		return randomGroundPlan;
	}

}
