package com.simibubi.mightyarchitect.control.design.partials;

import java.util.Map;

import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class TowerRoof extends Tower {

	@Override
	public Design fromNBT(CompoundNBT compound) {
		TowerRoof towerRoof = new TowerRoof();
		towerRoof.applyNBT(compound);
		towerRoof.radius = compound.getInt("Radius");
		towerRoof.defaultWidth = towerRoof.radius * 2 + 1;
		return towerRoof;
	}
	
	public DesignInstance create(BlockPos anchor) {
		return create(anchor, 0, size.getX(), size.getY());
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
	
	@Override
	public boolean fitsVertically(int height) {
		return true;
	}
	
}
