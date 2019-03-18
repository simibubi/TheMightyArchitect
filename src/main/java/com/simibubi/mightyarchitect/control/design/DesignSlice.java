package com.simibubi.mightyarchitect.control.design;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.simibubi.mightyarchitect.control.palette.Palette;

import net.minecraft.nbt.NBTTagCompound;
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

		return slice;
	}

	public Palette[][] getBlocks() {
		return blocks;
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
