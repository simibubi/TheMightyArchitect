package com.simibubi.mightyarchitect.control.design.partials;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.simibubi.mightyarchitect.control.design.DesignSlice;
import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class Design {

	protected BlockPos size;
	protected DesignSlice[] slices;
	protected Set<Integer> heights;
	protected int defaultHeight;
	protected int defaultWidth;
	protected int yShift;

	public abstract Design fromNBT(NBTTagCompound compound);
	
	protected void applyNBT(NBTTagCompound compound) {
		size = NBTUtil.getPosFromTag(compound.getCompoundTag("Size"));
		defaultWidth = size.getX();
		slices = new DesignSlice[size.getY()];
		
		defaultHeight = 0;
		yShift = 0;
		heights = ImmutableSet.of(0);
		NBTTagList sliceTagList = compound.getTagList("Layers", 10);
		
		for (int sliceIndex = 0; sliceIndex < slices.length; sliceIndex++) {
			DesignSlice slice = DesignSlice.fromNBT(sliceTagList.getCompoundTagAt(sliceIndex));
			defaultHeight = slice.adjustDefaultHeight(defaultHeight);
			heights = slice.adjustHeigthsList(heights);
			slices[sliceIndex] = slice;
			
			if (slice.getTrait() == DesignSliceTrait.MaskBelow)
				yShift -= 1;
		}		
	}
	
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		getBlocksShifted(instance, blocks, BlockPos.ORIGIN);
	}
	
	protected void getBlocksShifted(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks, BlockPos localShift) {
		BlockPos position = instance.localAnchor;
		BlockPos totalShift = localShift.add(0, yShift, 0);
		List<DesignSlice> toPrint = selectPrintedLayers(instance.height);

		for (int y = 0; y < toPrint.size(); y++) {
			DesignSlice layer = toPrint.get(y);
			for (int x = 0; x < size.getX(); x++) {
				for (int z = 0; z < size.getZ(); z++) {
					Palette key = layer.getBlocks()[z][x];
					if (key == null)
						continue;
					BlockPos pos = rotateAroundZero(new BlockPos(x, y, z).add(totalShift), instance.rotationY)
							.add(position);
					putBlock(blocks, pos, key, EnumFacing.fromAngle(instance.rotationY));
				}
			}
		}
	}
	
	protected List<DesignSlice> selectPrintedLayers(int targetHeight) {
		List<DesignSlice> toPrint = new LinkedList<>();
		int currentHeight = defaultHeight;
		for (DesignSlice slice : slices)
			currentHeight = slice.addToPrintedLayers(toPrint, currentHeight, targetHeight);
		return toPrint;
	}

	protected void putBlock(Map<BlockPos, PaletteBlockInfo> blocks, BlockPos pos, Palette palette, EnumFacing facing) {
		if (!blocks.containsKey(pos) || !blocks.get(pos).palette.isPrefferedOver(palette)) {
			blocks.put(pos, new PaletteBlockInfo(palette, facing));
		}
	}

	public String toString() {
		String heights = "Heights ";
		for (Integer integer : this.heights) {
			heights += integer + " ";
		}
		return String.format("Design with ") + heights;
	}
	
	public boolean fitsHorizontally(int width) {
		return this.defaultWidth == width;
	}

	public boolean fitsVertically(int height) {
		return heights.contains(Integer.valueOf(height));
	}

	public BlockPos rotateAroundZero(BlockPos in, int rotation) {
		return rotateAround(in, rotation, new BlockPos(0, 0, 0));
	}

	public BlockPos rotateAround(BlockPos in, int rotation, BlockPos origin) {
		BlockPos local = in.subtract(origin);
		int x = (rotation == 180) ? -local.getX()
				: (rotation == 90) ? -local.getZ() : (rotation == -90) ? local.getZ() : local.getX();
		int z = (rotation == 180) ? -local.getZ()
				: (rotation == 90) ? local.getX() : (rotation == -90) ? -local.getX() : local.getZ();
		BlockPos rotated = new BlockPos(x, local.getY(), z);
		return rotated.add(origin);
	}

	public DesignInstance create(BlockPos anchor, int rotation, int height) {
		return create(anchor, rotation, size.getX(), height);
	}

	public DesignInstance create(BlockPos anchor, int rotation, int width, int height) {
		return new DesignInstance(this, anchor, rotation, width, height, 0);
	}

	public class DesignInstance {

		BlockPos localAnchor;
		int rotationY, rotationZ;
		int width, height, depth;
		Design template;

		public DesignInstance(Design template, BlockPos anchor, int rotation, int width, int height, int depth) {
			this.template = template;
			this.localAnchor = anchor;
			this.height = height;
			this.rotationY = rotation;
			this.width = width;
			this.depth = depth;
		}

		public void getBlocks(Map<BlockPos, PaletteBlockInfo> blocks) {
			template.getBlocks(this, blocks);
		}
		
		public Design getTemplate() {
			return template;
		}


	}


}
