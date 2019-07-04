package com.simibubi.mightyarchitect.control.design.partials;

import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.control.design.DesignSlice;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class Roof extends Design {

	private static final int CROSS_ROOF_DEPTH = -1;

	@Override
	public Design fromNBT(CompoundNBT compound) {
		Roof roof = new Roof();
		roof.applyNBT(compound);
		roof.defaultWidth = compound.getInt("Roofspan");
		return roof;
	}

	public DesignInstance create(BlockPos anchor, int rotation, int depth) {
		return new DesignInstance(this, anchor, rotation, size.getX(), size.getY(), depth);
	}

	public DesignInstance createAsCross(BlockPos anchor, int rotation, int depth) {
		return new DesignInstance(this, anchor, rotation, size.getX(), size.getY(), CROSS_ROOF_DEPTH);
	}

	@Override
	public boolean fitsVertically(int height) {
		return true;
	}

	@Override
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		int xShift = (size.getX() - defaultWidth) / -2;
		int zShift = -2;

		// Drag roof blocks into depth
		BlockPos position = instance.localAnchor;
		List<DesignSlice> toPrint = selectPrintedLayers(instance.height);

		boolean crossRoof = instance.depth == CROSS_ROOF_DEPTH;
		int depth = crossRoof ? (size.getX() + (8 - defaultWidth)) / 2 : instance.depth;
		BlockPos totalShift = new BlockPos(xShift, yShift, zShift);

		for (int y = 0; y < toPrint.size(); y++) {
			DesignSlice layer = toPrint.get(y);

			for (int x = 0; x < size.getX(); x++) {
				int currentDepth = (size.getX() - 1) / 2 - Math.abs((size.getX() - 1) / 2 - x);

				for (int z = -(depth + zShift + 1); z < 0; z++) {
					PaletteBlockInfo block = layer.getBlockAt(x, 0, instance.rotationY);

					if (block == null)
						continue;
					if (crossRoof && depth - z + zShift > currentDepth)
						continue;

					BlockPos pos = position
							.add(rotateAroundZero(new BlockPos(x, y, z).add(totalShift), instance.rotationY));
					putBlock(blocks, pos, block);
				}
								
				for (int z = 0; z < size.getZ(); z++) {
					PaletteBlockInfo block = layer.getBlockAt(x, z, instance.rotationY);

					if (block == null)
						continue;
					if (crossRoof && depth - z + zShift > currentDepth)
						continue;

					BlockPos pos = rotateAroundZero(new BlockPos(x, y, z).add(totalShift), instance.rotationY)
							.add(position);
					putBlock(blocks, pos, block);
				}
			}
		}

	}

}
