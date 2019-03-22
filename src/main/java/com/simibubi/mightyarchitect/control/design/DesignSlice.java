package com.simibubi.mightyarchitect.control.design;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;

public class DesignSlice {

	public enum DesignSliceTrait implements IStringSerializable {
		Standard("-> Use this slice once"), 
		CloneOnce("-> Duplicate this slice if necessary"), 
		CloneThrice("-> Duplicate up to 3 times"), 
		Optional("-> Ignore slice if necessary"), 
		MaskAbove("-> Slice does not count towards effective Height"), 
		MaskBelow("-> Add this slice onto lower layers");

		private String description;
		
		private DesignSliceTrait(String description) {
			this.description = description;
		}
		
		@Override
		public String getName() {
			return name().toLowerCase();
		}
		
		public String getDescription() {
			return description;
		}
	}

	private DesignSliceTrait trait;
	private Palette[][] blocks;
	private BlockOrientation[][] orientations;

	public static DesignSlice fromNBT(NBTTagCompound sliceTag) {
		DesignSlice slice = new DesignSlice();
		slice.trait = DesignSliceTrait.valueOf(sliceTag.getString("Trait"));

		String[] strips = sliceTag.getString("Blocks").split(",");
		int width = strips[0].length();
		int length = strips.length;
		slice.blocks = new Palette[length][width];

		for (int z = 0; z < length; z++) {
			String strip = strips[z];
			for (int x = 0; x < width; x++) {
				char charAt = strip.charAt(x);
				if (charAt != ' ')
					slice.blocks[z][x] = Palette.getByChar(charAt);
			}
		}
		
		slice.orientations = new BlockOrientation[length][width];
		if (sliceTag.hasKey("Facing")) {
			strips = sliceTag.getString("Facing").split(",");
			
			for (int z = 0; z < length; z++) {
				String strip = strips[z];
				for (int x = 0; x < width; x++) {
					char charAt = strip.charAt(x);
					slice.orientations[z][x] = BlockOrientation.valueOf(charAt);
				}
			}
			
		} else {
			for (int z = 0; z < length; z++) {
				Arrays.fill(slice.orientations[z], BlockOrientation.NONE);
			}
		}

		return slice;
	}
	
	public PaletteBlockInfo getBlockAt(int x, int z, int rotation) {
		return getBlockAt(x, z, rotation, false);
	}
	
	public PaletteBlockInfo getBlockAt(int x, int z, int rotation, boolean mirrorX) {
		Palette palette = blocks[z][x];
		if (palette == null)
			return null;
		
		BlockOrientation blockOrientation = orientations[z][x];
		if (!blockOrientation.hasFacing())
			blockOrientation = BlockOrientation.valueOf(blockOrientation.getHalf(), EnumFacing.SOUTH);		
		
		BlockOrientation withRotation = blockOrientation.withRotation(rotation);
		PaletteBlockInfo paletteBlockInfo = new PaletteBlockInfo(palette, withRotation);			
		
		if (orientations[z][x].hasFacing() && orientations[z][x].getFacing().getAxis() != Axis.Y)
			paletteBlockInfo.forceAxis = true;
		
		if (rotation % 180 == 0)
			paletteBlockInfo.mirrorZ = mirrorX;
		else 
			paletteBlockInfo.mirrorX = mirrorX;
		return paletteBlockInfo;
	}

	public DesignSliceTrait getTrait() {
		return trait;
	}

	public Set<Integer> adjustHeigthsList(Set<Integer> heightsList) {
		Set<Integer> newHeights = new HashSet<>();
		for (Integer integer : heightsList) {
			switch (trait) {
			case Standard:
				newHeights.add(integer + 1);
				break;
			case CloneOnce:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				break;
			case CloneThrice:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				newHeights.add(integer + 3);
				newHeights.add(integer + 4);
				break;
			case Optional:
				newHeights.add(integer);
				newHeights.add(integer + 1);
				break;
			case MaskAbove:
			case MaskBelow:
				newHeights.add(integer);
				break;
			}
		}
		return newHeights;
	}

	public int adjustDefaultHeight(int defaultHeight) {
		switch (trait) {
		case MaskAbove:
		case MaskBelow:
			return defaultHeight;
		default:
			return defaultHeight + 1;
		}
	}

	public int addToPrintedLayers(List<DesignSlice> toPrint, int currentHeight, int targetHeight) {
		switch (trait) {
		case MaskAbove:
		case MaskBelow:
		case Standard:
			toPrint.add(this);
			return currentHeight;
		case Optional:
			if (currentHeight > targetHeight) {
				return currentHeight - 1;
			} else {
				toPrint.add(this);
				return currentHeight;				
			}
		case CloneOnce:
			toPrint.add(this);
			if (currentHeight < targetHeight) {
				toPrint.add(this);
				return currentHeight + 1;
			}
			return currentHeight;
		case CloneThrice:
			toPrint.add(this);
			int i = 0;
			for (; i < 3 && currentHeight + i < targetHeight; i++) {
				toPrint.add(this);
			}
			return currentHeight + i;
		default:
			return currentHeight;
		}
	}

}
