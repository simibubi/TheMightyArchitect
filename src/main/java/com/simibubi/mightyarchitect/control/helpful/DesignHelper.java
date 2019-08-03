package com.simibubi.mightyarchitect.control.helpful;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignPicker.RoomDesignMapping;
import com.simibubi.mightyarchitect.control.design.DesignQuery;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.TemporaryDesignCache;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.control.design.partials.Corner;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;
import com.simibubi.mightyarchitect.control.design.partials.FlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.Roof;
import com.simibubi.mightyarchitect.control.design.partials.Tower;
import com.simibubi.mightyarchitect.control.design.partials.TowerFlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.TowerRoof;

import net.minecraft.util.math.BlockPos;

public class DesignHelper {

	/**
	 * Finds a random design that fulfils the provided requirements. Returns a
	 * fallback design if no fitting design was found.
	 */
	public static Design pickRandom(DesignQuery query, Random rand) {
		Design design = pickRandomNoFallback(query, rand);

		if (design == null && query.fallback) {
			return pickRandomNoFallback(query.withTheme(ThemeStorage.IncludedThemes.Fallback.theme), rand);
		}

		return design;
	}

	private static Design pickRandomNoFallback(DesignQuery query, Random rand) {
		List<Design> remainingDesigns = new ArrayList<>(query.theme.getDesigns(query.layer, query.type));
		while (!remainingDesigns.isEmpty()) {
			int index = rand.nextInt(remainingDesigns.size());

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
	public static void addCuboid(TemporaryDesignCache designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, Room room) {

		Design facadeA = null;
		Design facadeB = null;
		BlockPos start = room.getOrigin();
		BlockPos size = room.getSize();
		int width = size.getX();
		int height = size.getY();
		int length = size.getZ();

		Design wallA = null, wallB = null, corner = null;
		boolean facadeAlongX = width <= length;
		boolean facadeAlongZ = width >= length;

		// Find cached entry
		if (designProvider.hasCachedRoom(room)) {
			RoomDesignMapping designs = designProvider.getCachedRoom(room);
			wallA = designs.wall1;
			wallB = designs.wall2;
			corner = designs.corner;

			// Size changed
			if (!wallA.fitsHorizontally(width - 2) || !wallA.fitsVertically(height))
				wallA = null;
			if (!wallB.fitsHorizontally(length - 2) || !wallB.fitsVertically(height))
				wallB = null;
			if (!corner.fitsVertically(height))
				corner = null;

		}

		// Find new designs
		if (wallA == null || wallB == null || corner == null) {
			DesignQuery facadeQuery = new DesignQuery(theme, layer, DesignType.FACADE).withHeight(height)
					.withoutFallback();
			DesignQuery wallQuery = new DesignQuery(theme, layer, DesignType.WALL).withHeight(height);
			DesignQuery cornerQuery = new DesignQuery(theme, layer, DesignType.CORNER).withHeight(height);

			if (wallA == null) {
				if (facadeAlongX)
					facadeA = designProvider.find(facadeQuery.withWidth(width - 2));
				wallA = (facadeA != null) ? facadeA : designProvider.find(wallQuery.withWidth(width - 2));
			}

			if (wallB == null) {
				if (facadeAlongZ)
					facadeB = designProvider.find(facadeQuery.withWidth(length - 2));
				wallB = (facadeB != null) ? facadeB : designProvider.find(wallQuery.withWidth(length - 2));
			}

			if (corner == null)
				corner = designProvider.find(cornerQuery);

			designProvider.cacheRoom(room, new RoomDesignMapping(wallA, wallB, corner));
		}

		// Open Arcs layer - try to have similar walls
		if (layer == DesignLayer.Open) {
			if (wallA.fitsHorizontally(length - 2))
				wallB = wallA;
			else if (wallB.fitsHorizontally(width - 2))
				wallA = wallB;
		}

		BlockPos cornerZ = start.add(0, 0, length - 1);
		BlockPos cornerXZ = start.add(width - 1, 0, length - 1);
		BlockPos cornerX = start.add(width - 1, 0, 0);

		if (wallA != null && wallB != null && corner != null) {
			designList.add(wall(wallA, cornerZ, cornerXZ, height));
			designList.add(wall(wallB, cornerXZ, cornerX, height));
			designList.add(wall(wallA, cornerX, start, height));
			designList.add(wall(wallB, start, cornerZ, height));

			designList.add(corner(corner, start, facadeAlongX ? 135 : 45, height, !facadeAlongX));
			designList.add(corner(corner, cornerZ, facadeAlongX ? -45 : 45, height, facadeAlongX));
			designList.add(corner(corner, cornerXZ, facadeAlongX ? -45 : -135, height, !facadeAlongX));
			designList.add(corner(corner, cornerX, facadeAlongX ? 135 : -135, height, facadeAlongX));
		}

	}

	public static void addTower(TemporaryDesignCache designProvider, List<DesignInstance> designList, DesignTheme theme,
			DesignLayer layer, Room room) {
		BlockPos start = room.getOrigin();
		BlockPos size = room.getSize();
		int diameter = size.getX();
		int height = size.getY();
		Design tower = null;

		// Find cached entry
		if (designProvider.hasCachedRoom(room)) {
			tower = designProvider.getCachedRoom(room).wall1;

			// Size changed
			if (!tower.fitsHorizontally(diameter) || !tower.fitsVertically(height))
				tower = null;
		}

		// Find new designs
		if (tower == null) {
			DesignQuery towerQuery = new DesignQuery(theme, layer, DesignType.TOWER).withWidth(diameter)
					.withHeight(height);
			tower = designProvider.find(towerQuery);
			designProvider.cacheRoom(room, new RoomDesignMapping(tower));
		}

		if (tower == null)
			return;

		designList.add(tower(tower, start, height));
	}

	public static void addTowerRoof(TemporaryDesignCache designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, Stack stack, boolean flat) {
		Room highest = stack.highest();
		BlockPos start = highest.getOrigin().up(highest.height);
		BlockPos size = highest.getSize();
		int diameter = size.getX();
		Design roof = null;

		// Find cached entry
		if (designProvider.hasCachedRoof(stack)) {
			roof = designProvider.getCachedRoof(stack);

			// Size or Type changed
			boolean typeChanged = stack.getRoofType().getDesign().getClass() != roof.getClass();
			if (!roof.fitsHorizontally(diameter) || typeChanged)
				roof = null;
		}

		if (roof == null) {
			DesignType type = flat ? DesignType.TOWER_FLAT_ROOF : DesignType.TOWER_ROOF;
			DesignQuery roofQuery = new DesignQuery(theme, layer, type).withWidth(diameter);
			roof = designProvider.find(roofQuery);
			designProvider.cacheRoof(stack, roof);
		}

		if (roof == null)
			return;

		designList.add(flat ? towerFlatRoof(roof, start) : towerRoof(roof, start));
	}

	/**
	 * Creates a roof with two facades sitting on the shorter sides of the cuboid
	 */
	public static void addNormalRoof(TemporaryDesignCache designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, Stack stack) {
		Room highest = stack.highest();
		BlockPos start = highest.getOrigin().up(highest.height);
		BlockPos size = highest.getSize();
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);
		Design roof = null;

		if (designProvider.hasCachedRoof(stack)) {
			roof = designProvider.getCachedRoof(stack);

			// Size or Type changed
			if (!roof.fitsHorizontally(width) || roof.getClass() != highest.roofType.getDesign().getClass())
				roof = null;
		}

		if (roof == null) {
			roof = designProvider.find(new DesignQuery(theme, layer, DesignType.ROOF).withWidth(width));
			designProvider.cacheRoof(stack, roof);
		}

		if (roof == null)
			return;

		designList.add(roof(roof, south ? start : cornerX, south ? 90 : 180, depth - 4));
		designList.add(roof(roof, south ? cornerXZ : cornerZ, south ? -90 : 0, depth - 4));
	}

	/**
	 * Creates a roof with facades on all sides of the cuboid
	 */
	public static void addNormalCrossRoof(TemporaryDesignCache designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, Stack stack) {
		Room highest = stack.highest();
		BlockPos start = highest.getOrigin().up(highest.height);
		BlockPos size = highest.getSize();
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);
		Design roof = null;

		if (designProvider.hasCachedRoof(stack)) {
			roof = designProvider.getCachedRoof(stack);

			// Size or Type changed
			if (!roof.fitsHorizontally(width) || roof.getClass() != highest.roofType.getDesign().getClass())
				roof = null;
		}

		if (roof == null) {
			roof = designProvider.find(new DesignQuery(theme, layer, DesignType.ROOF).withWidth(width));
			designProvider.cacheRoof(stack, roof);
		}

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
	public static void addFlatRoof(TemporaryDesignCache designProvider, List<DesignInstance> designList,
			DesignTheme theme, DesignLayer layer, Stack stack) {
		Room highest = stack.highest();
		BlockPos start = highest.getOrigin().up(highest.height);
		BlockPos size = highest.getSize();
		boolean south = size.getZ() < size.getX();
		int depth = south ? size.getX() : size.getZ();
		int width = south ? size.getZ() : size.getX();

		BlockPos cornerZ = start.add(0, 0, size.getZ() - 1);
		BlockPos cornerXZ = start.add(size.getX() - 1, 0, size.getZ() - 1);
		BlockPos cornerX = start.add(size.getX() - 1, 0, 0);
		Design flatroof = null;

		if (designProvider.hasCachedRoof(stack)) {
			flatroof = designProvider.getCachedRoof(stack);

			// Size or Type changed
			if (!flatroof.fitsHorizontally(width) || flatroof.getClass() != highest.roofType.getDesign().getClass())
				flatroof = null;
		}

		if (flatroof == null) {
			flatroof = designProvider.find(new DesignQuery(theme, layer, DesignType.FLAT_ROOF).withWidth(width));
			designProvider.cacheRoof(stack, flatroof);
		}

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
	public static DesignInstance corner(Design design, BlockPos pos, int angle, int height, boolean flip) {
		return ((Corner) design).create(pos, angle + 45, height, flip);
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
	 * Creates a quadroof part of the specified design. Valid angles: 0, 90, 180,
	 * -90
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

	public static DesignInstance tower(Design design, BlockPos pos, int height) {
		return ((Tower) design).create(pos, height);
	}

	public static DesignInstance towerRoof(Design design, BlockPos pos) {
		return ((TowerRoof) design).create(pos);
	}

	public static DesignInstance towerFlatRoof(Design design, BlockPos pos) {
		return ((TowerFlatRoof) design).create(pos);
	}
}
