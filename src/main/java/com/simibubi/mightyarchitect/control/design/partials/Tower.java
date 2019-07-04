package com.simibubi.mightyarchitect.control.design.partials;

import java.util.Map;

import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class Tower extends Design {

	public int radius;
	
	@Override
	public Design fromNBT(CompoundNBT compound) {
		Tower tower = new Tower();
		tower.applyNBT(compound);
		tower.radius = compound.getInt("Radius");
		tower.defaultWidth = tower.radius * 2 + 1;
		return tower;
	}
	
	public DesignInstance create(BlockPos anchor, int height) {
		return create(anchor, 0, size.getX(), height);
	}
	
	@Override
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		int shift = (size.getX() - defaultWidth) / 2;
		getBlocksShifted(instance, blocks, new BlockPos(-shift, 0, -shift));
	}

	@Override
	public String toString() {
		return super.toString() + "\nRadius " + radius;
	}
	
	@Override
	public boolean fitsHorizontally(int width) {
		return width == defaultWidth;
	}
	
	


}
