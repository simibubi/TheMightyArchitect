package com.simibubi.mightyarchitect.buildomatico.helpful;

import java.util.Map;

import com.simibubi.mightyarchitect.buildomatico.Palette;

import net.minecraft.util.math.BlockPos;

public class BuildingHelper {

	public static void circle(Map<BlockPos, Palette> building, BlockPos center, int radius, Palette block) {
		int z = 0;
		int x = radius;
		int p = (5 - radius * 4) / 4;

		building.put(center.add(x, 0, z), block);

		while (z < x) {
			z++;
			if (p < 0) {
				p += 2 * z + 1;
			} else {
				x--;
				p += 2 * (z - x) + 1;
			}
			building.put(center.add(x, 0, z), block);
		}
	}
	
	public static void thickCircle(Map<BlockPos, Palette> building, BlockPos center, int radius, Palette block) {
		int z = 0;
		int x = radius;
		int p = (5 - radius * 4) / 4;
		
		building.put(center.add(x, 0, z), block);
		
		while (z < x) {
			z++;
			if (p < 0) {
				p += 2 * z + 1;
			} else {
				building.put(center.add(x, 0, z), block);
				x--;
				p += 2 * (z - x) + 1;
			}
			building.put(center.add(x, 0, z), block);
		}
	}
	
	public static void filledCircle(Map<BlockPos, Palette> building, BlockPos center, int radius, Palette block) {
		int z = 0;
		int x = radius;
		int p = (5 - radius * 4) / 4;
		
		for (int i = 0; i <= x; i++)
			building.put(center.add(i, 0, z), block);
		
		while (z < x) {
			z++;
			if (p < 0) {
				p += 2 * z + 1;
			} else {
				x--;
				p += 2 * (z - x) + 1;
			}
			for (int i = 0; i <= x; i++)
				building.put(center.add(i, 0, z), block);
		}
	}

}
