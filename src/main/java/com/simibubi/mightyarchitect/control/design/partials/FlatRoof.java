package com.simibubi.mightyarchitect.control.design.partials;

import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.control.design.DesignSlice;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;

public class FlatRoof extends Design {

	protected int margin;
	
	@Override
	public Design fromNBT(CompoundNBT compound) {
		FlatRoof flatRoof = new FlatRoof();
		flatRoof.applyNBT(compound);
		
		flatRoof.margin = compound.getInt("Margin");
		flatRoof.defaultWidth = (flatRoof.defaultWidth - flatRoof.margin) * 2 - 1;
		
		return flatRoof;
	}
	
	@Override
	public DesignInstance create(BlockPos anchor, int rotation, int width, int depth) {
		return new DesignInstance(this, anchor, rotation, width, size.getY(), depth);
	}
	
	@Override
	public boolean fitsVertically(int height) {
		return true;
	}
	
	@Override
	public boolean fitsHorizontally(int width) {
		return width >= defaultWidth;
	}
	
	@Override
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		int xShift = margin;
		int zShift = margin;
		
		// Drag roof blocks into depth
		BlockPos position = instance.localAnchor;
		List<DesignSlice> printedSlices = selectPrintedLayers(instance.height);
		
		for (int y = 0; y < printedSlices.size(); y++) {
			for (int x = 0; x < size.getX(); x++) {
				for (int z = size.getZ() - 1; z < instance.depth - size.getZ() + 2 * margin; z++) {
					PaletteBlockInfo block = printedSlices.get(y).getBlockAt(x, 0, instance.rotationY);
					if (block == null) continue;
					BlockPos pos = position.offset(rotateAroundZero(new BlockPos(x - xShift, y + yShift, -z + zShift), instance.rotationY));
					putBlock(blocks, pos, block); 
				}
			}
		}
		
		// Drag roof blocks into width
		for (int y = 0; y < printedSlices.size(); y++) {
			for (int x = size.getX(); x <= instance.width - (size.getX() - margin - 1); x++) {
				for (int z = -(instance.depth - 2 * size.getZ() + zShift + 1); z < size.getZ(); z++) {
					PaletteBlockInfo block = printedSlices.get(y).getBlockAt(size.getX() - 1, Math.max(z, 0), instance.rotationY);
					if (block == null) continue;
					BlockPos pos = position.offset(rotateAroundZero(new BlockPos(x - xShift, y + yShift, z + zShift - size.getZ() + 1), instance.rotationY));
					putBlock(blocks, pos, block); 
				}
			}
		}
		
		// Print the facade
		BlockPos totalShift = new BlockPos(-xShift, yShift, zShift - size.getZ() + 1);
		BlockPos mirrorShift = new BlockPos(xShift, yShift, zShift - size.getZ() + 1);
		for (int y = 0; y < printedSlices.size(); y++) {
			DesignSlice layer = printedSlices.get(y);
			for (int x = 0; x < size.getX(); x++) {
				for (int z = 0; z < size.getZ(); z++) {
					PaletteBlockInfo block = layer.getBlockAt(x, z, instance.rotationY, false);
					PaletteBlockInfo blockMirrored = layer.getBlockAt(x, z, instance.rotationY, true);
					if (block == null)
						continue;
					BlockPos pos = rotateAroundZero(new BlockPos(x, y, z).offset(totalShift), instance.rotationY)
							.offset(position);
					BlockPos posMirrored = rotateAroundZero(new BlockPos(instance.width - x - 1, y, z).offset(mirrorShift), instance.rotationY)
							.offset(position);
					putBlock(blocks, pos, block);
					putBlock(blocks, posMirrored, blockMirrored);
				}
			}
		}
	}

}
