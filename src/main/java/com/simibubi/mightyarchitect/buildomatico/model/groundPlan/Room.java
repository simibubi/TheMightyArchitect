package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class Room extends Cuboid {

	public DesignLayer designLayer;
	public DesignType roofType;
	public char styleGroup;
	public boolean secondaryPalette;
	public int layer;
	
	public Room(BlockPos origin, BlockPos size) {
		this(origin, size.getX(), size.getY(), size.getZ());
	}

	public Room(BlockPos origin, int width, int height, int length) {
		super(origin, width, height, length);
		styleGroup = 'A';
		designLayer = DesignLayer.Regular;
		roofType = DesignType.ROOF;
		secondaryPalette = false;
	}
	
	public Room clone() {
		Room clone = new Room(getOrigin(), getSize());
		clone.styleGroup = styleGroup;
		clone.roofType = roofType;
		clone.secondaryPalette = secondaryPalette;
		return clone;
	}

	public Room getInterior() {
		Room clone = clone();
		clone.x += 1;
		clone.z += 1;
		clone.width -= 2;
		clone.length -= 2;
		clone.secondaryPalette = secondaryPalette;
		return clone;
	}

	public Room stack() {
		Room clone = clone();
		clone.y += height;
		this.roofType = DesignType.NONE;
		return clone;
	}

	public Axis getOrientation() {
		return (width > length) ? Axis.X : Axis.Z;
	}


}
