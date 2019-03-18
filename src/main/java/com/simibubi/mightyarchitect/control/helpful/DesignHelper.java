package com.simibubi.mightyarchitect.control.helpful;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignQuery;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.StyleGroupManager.StyleGroupDesignProvider;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.design.partials.FlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.Roof;
import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;

import net.minecraft.util.math.BlockPos;

public class DesignHelper {

	/**
	 * Finds a random design that fulfils the provided requirements. Returns null if
	 * no fitting design was found.
	 */
	public static Design pickRandom(DesignQuery query) {
		List<Design> remainingDesigns = new ArrayList<>(query.theme.getDesigns(query.layer, query.type));
		Random dice = new Random();
		while (!remainingDesigns.isEmpty()) {
			int index = dice.nextInt(remainingDesigns.size());

			Design chosen = remainingDesigns.get(index);
			if (query.isWidthIgnored() || chosen.fitsHorizontally(query.desiredWidth)) {
				if (query.isHeightIgnored() || chosen.fitsVertically(query.desiredHeight)) {
					return chosen;
				}
			}

			remainingDesigns.remove(index);
		}
		return null;
	}

	/**
	 * Creates a closed room around the specified cuboid using 4 walls and 4 corners
	 */
	public static void addCuboid(StyleGroupDesignProvider designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, BlockPos start, BlockPos size) {

		Design facadeA = null;
		Design facadeB = null;
		int width = size.getX();
		int height = size.getY();
		int length = size.getZ();

		DesignQuery facadeQuery = new DesignQuery(theme, layer, DesignType.FACADE).withHeight(height);
		DesignQuery wallQuery = new DesignQuery(theme, layer, DesignType.WALL).withHeight(height);

		DesignLayer cornerLayer = (layer != DesignLayer.Open) ? layer : DesignLayer.Regular;
		DesignQuery cornerQuery = new DesignQuery(theme, cornerLayer, DesignType.CORNER).withHeight(height);

		if (width <= length)
			facadeA = designProvider.find(facadeQuery.withWidth(width - 2));
		if (length <= width)
			facadeB = designProvider.find(facadeQuery.withWidth(length - 2));

		Design wallA = (facadeA != null) ? facadeA : designProvider.find(wallQuery.withWidth(width - 2));
		Design wallB = (facadeB != null) ? facadeB : designProvider.find(wallQuery.withWidth(length - 2));
		Design corner = designProvider.find(cornerQuery);

		BlockPos cornerZ = start.add(0, 0, length - 1);
		BlockPos cornerXZ = start.add(width - 1, 0, length - 1);
		BlockPos cornerX = start.add(width - 1, 0, 0);

		if (wallA != null && wallB != null && corner != null) {
			designList.add(wall(wallA, cornerZ, cornerXZ, height));
			designList.add(wall(wallB, cornerXZ, cornerX, height));
			designList.add(wall(wallA, cornerX, start, height));
			designList.add(wall(wallB, start, cornerZ, height));
			designList.add(corner(corner, start, 135, height));
			designList.add(corner(corner, cornerZ, 45, height));
			designList.add(corner(corner, cornerXZ, -45, height));
			designList.add(corner(corner, cornerX, -135, height));
		}

	}

	/**
	 * Creates a roof with two facades sitting on the shorter sides of the cuboid
	 */
	public static void addNormalRoof(StyleGroupDesignProvider designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, BlockPos start, BlockPos size) {
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);

		Design roof = designProvider.find(new DesignQuery(theme, layer, DesignType.ROOF).withWidth(width));

		if (roof == null)
			return;

		designList.add(roof(roof, south ? start : cornerX, south ? 90 : 180, depth - 4));
		designList.add(roof(roof, south ? cornerXZ : cornerZ, south ? -90 : 0, depth - 4));
	}

	/**
	 * Creates a roof with facades on all sides of the cuboid 
	 */
	public static void addNormalCrossRoof(StyleGroupDesignProvider designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, BlockPos start, BlockPos size) {
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);

		Design roof = designProvider.find(new DesignQuery(theme, layer, DesignType.ROOF).withWidth(width));

		if (roof == null)
			return;

		designList.add(quadRoof(roof, start, 90, depth - 4));
		designList.add(quadRoof(roof, cornerXZ, -90, depth - 4));
		designList.add(quadRoof(roof, cornerX, 180, depth - 4));
		designList.add(quadRoof(roof, cornerZ, 0, depth - 4));
	}

	/**
	 * Creates a flat roof on top of the cuboid
	 */
	public static void addFlatRoof(StyleGroupDesignProvider designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, BlockPos start, BlockPos size) {
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);

		Design flatroof = designProvider.find(new DesignQuery(theme, layer, DesignType.FLAT_ROOF).withWidth(width));
		
		if (flatroof == null) 
			return;
			
		designList.add(flatroof(flatroof, south ? start : cornerX, south ? 90 : 180, width, depth));
		designList.add(flatroof(flatroof, south ? cornerXZ : cornerZ, south ? -90 : 0, width, depth));

	}

	/**
	 * Adds a wall in the specified design between the corner points facing z+ when
	 * x1 < x2
	 */
	public static DesignInstance wall(Design design, BlockPos corner1, BlockPos corner2, int height) {
		int xDiff = corner2.getX() - corner1.getX();
		int zDiff = corner2.getZ() - corner1.getZ();
		int xStep = (int) Math.signum(xDiff);
		int zStep = (int) Math.signum(zDiff);
		corner1 = corner1.add(xStep, 0, zStep);
		int width = Math.abs((xDiff == 0) ? zDiff : xDiff) - 1;
		int rotation = ((xDiff == 0) ? ((zDiff > 0) ? 90 : -90) : ((xDiff > 0) ? 0 : 180));
		return design.create(corner1, rotation, width, height);
	}

	/**
	 * Creates a corner of the specified design. Valid angles: 45, 135, -135, -45,
	 */
	public static DesignInstance corner(Design design, BlockPos pos, int angle, int height) {
		return design.create(pos, angle + 45, height);
	}

	/**
	 * Creates a trim of the specified design. Valid angles: 0, 90, 180, -90
	 */
	public static DesignInstance trim(Design design, BlockPos pos, int angle, int height) {
		return design.create(pos, angle, height);
	}

	/**
	 * Creates a roof of the specified design. Valid angles: 0, 90, 180, -90
	 */
	public static DesignInstance roof(Design design, BlockPos pos, int angle, int depth) {
		return ((Roof) design).create(pos, angle, depth);
	}
	
	/**
	 * Creates a quadroof part of the specified design. Valid angles: 0, 90, 180, -90
	 */
	public static DesignInstance quadRoof(Design design, BlockPos pos, int angle, int depth) {
		return ((Roof) design).createAsCross(pos, angle, depth);
	}

	/**
	 * Creates a faltroof of the specified design. Valid angles: 0, 90, 180, -90
	 */
	public static DesignInstance flatroof(Design design, BlockPos pos, int angle, int width, int depth) {
		return ((FlatRoof) design).create(pos, angle, width, depth);
	}
}
