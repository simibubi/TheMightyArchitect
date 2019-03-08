package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class Roof extends Design {

	public Roof(List<String> definition) {
		super(definition);
		String[] keyWords = definition.get(0).split(" ");
		defaultWidth = (keyWords.length >= 5) ? Integer.parseInt(keyWords[4]) : 0;
	}

	public DesignInstance create(BlockPos anchor, int rotation, int depth) {
		return new DesignInstance(this, anchor, rotation, size.getX(), size.getY(), depth);
	}

	@Override
	public Type getType() {
		return Type.ROOF;
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
		List<Layer> toPrint = selectPrintedLayers(instance);
		for (int y = 0; y < toPrint.size(); y++) {
			for (int x = 0; x < size.getX(); x++) {
				for (int z = 0; z < instance.depth + zShift + 1; z++) {
					Palette key = toPrint.get(y).blocks[0][x];
					if (key == null) continue;
					BlockPos pos = position.add(rotateAroundZero(new BlockPos(x + xShift, y + yShift, -z + zShift), instance.rotationY));
					putBlock(blocks, pos, key, EnumFacing.fromAngle(instance.rotationY)); 
				}
			}
		}
		
		// Print the facade
		BlockPos shift = new BlockPos(xShift, 0, zShift);
		super.getBlocksShifted(instance, blocks, shift);
	}
	
	@Override
	protected Cuboid getBounds(DesignInstance instance) {
		Cuboid bounds = new Cuboid(rotateAroundZero(instance.localAnchor, instance.rotationY), rotateAroundZero(new BlockPos(instance.width, instance.height, -instance.depth), instance.rotationY));
		return bounds;
	}

}
