package com.simibubi.mightyarchitect.foundation.utility;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

public class BuildingHelper {

	public static Set<BlockPos> getCircle(BlockPos center, int radius) {
		Set<BlockPos> result = new HashSet<>();
		int z = 0;
		int x = radius;
		int p = (5 - radius * 4) / 4;

		result.add(center.offset(x, 0, z));
		result.add(center.offset(-x, 0, z));
		result.add(center.offset(-z, 0, x));
		result.add(center.offset(z, 0, -x));

		while (z < x) {
			z++;
			if (radius < 5 && p <= 0 || radius >= 5 && p < 0) {
				p += 2 * z + 1;
			} else {
				x--;
				p += 2 * (z - x) + 1;
			}
			result.add(center.offset(x, 0, z));
			result.add(center.offset(-x, 0, -z));
			result.add(center.offset(-x, 0, z));
			result.add(center.offset(x, 0, -z));
			
			result.add(center.offset(z, 0, x));
			result.add(center.offset(-z, 0, -x));
			result.add(center.offset(-z, 0, x));
			result.add(center.offset(z, 0, -x));
		}
		return result;
	}


}
