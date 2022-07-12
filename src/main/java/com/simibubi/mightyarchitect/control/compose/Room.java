package com.simibubi.mightyarchitect.control.compose;

import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;

public class Room extends Cuboid {

	public DesignLayer designLayer;
	public DesignType roofType;
	public char styleGroup;
	public boolean secondaryPalette;
	public boolean quadFacadeRoof;
	public int layer;

	public Room(BlockPos origin, BlockPos size) {
		this(origin, size.getX(), size.getY(), size.getZ());
	}

	public Room(BlockPos origin, int width, int height, int length) {
		super(origin, width, height, length);
		styleGroup = 'A';
		designLayer = DesignLayer.Regular;
		roofType = DesignType.ROOF;
		quadFacadeRoof = Math.abs(width) == Math.abs(length);
		secondaryPalette = false;
	}

	public Room clone() {
		Room clone = new Room(getOrigin(), getSize());
		clone.styleGroup = styleGroup;
		clone.roofType = roofType;
		clone.quadFacadeRoof = quadFacadeRoof;
		clone.secondaryPalette = secondaryPalette;
		return clone;
	}

	public Room getInterior() {
		Room clone = clone();
		clone.x += 1;
		clone.z += 1;
		clone.width -= 2;
		clone.length -= 2;
		clone.designLayer = designLayer;
		clone.secondaryPalette = secondaryPalette;
		return clone;
	}

	public Room stack(boolean exactCopy) {
		Room clone = clone();
		clone.y += height;
		if (!exactCopy)
			clone.height = Math.max(4, height);
		this.roofType = DesignType.NONE;
		this.quadFacadeRoof = false;
		return clone;
	}

	public Axis getOrientation() {
		return (width > length) ? Axis.X : Axis.Z;
	}

}
