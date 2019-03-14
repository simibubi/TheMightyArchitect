package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class Room extends Cuboid {

	public DesignLayer designLayer;
	public DesignType roofType;
	public char styleGroup;
	public boolean secondaryPalette;
	public int layer;
	
	private Map<EnumFacing, Set<Room>> attachedRooms;

	public Room(BlockPos origin, BlockPos size) {
		this(origin, size.getX(), size.getY(), size.getZ());
	}

	public Room(BlockPos origin, int width, int height, int length) {
		super(origin, width, height, length);
		attachedRooms = new HashMap<>();
		styleGroup = 'A';
		designLayer = DesignLayer.None;
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

	public void attachTo(Room other, EnumFacing side, int shift) {
		moveToAttach(other, side, shift);
		other.putAttached(side, this);
		putAttached(side.getOpposite(), other);
	}

	public void moveToAttach(Room other, EnumFacing side, int shift) {
		if (side != EnumFacing.EAST && side != EnumFacing.WEST)
			centerOnOthersX(other, shift);

		if (side != EnumFacing.NORTH && side != EnumFacing.SOUTH)
			centerOnOthersZ(other, shift);

		switch (side) {
		case WEST:
			this.x = other.x + other.width;
			break;
		case EAST:
			this.x = other.x - this.width;
			break;
		case SOUTH:
			this.z = other.z + other.length;
			break;
		case NORTH:
			this.z = other.z - this.length;
			break;
		case UP:
			this.y = other.y + other.height;
			break;
		case DOWN:
			this.y = other.y - this.height;
			break;
		default:
		}
	}

	private void centerOnOthersZ(Room other, int shift) {
		this.z = other.z + shift + (other.length - this.length) / 2;
	}

	private void centerOnOthersX(Room other, int shift) {
		this.x = other.x + shift + (other.width - this.width) / 2;
	}

	public Room getInterior() {
		Room clone = clone();
		clone.x += 1;
		clone.z += 1;
		clone.width -= 2;
		clone.length -= 2;
		clone.secondaryPalette = secondaryPalette;

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			for (Room attached : getAttached(side)) {
				boolean positive = side.getAxisDirection() == AxisDirection.POSITIVE;
				if (side.getAxis() == Axis.X) {
					if (this.length <= attached.length) {
						clone.x += positive ? -2 : 0;
						clone.width += 2;
					}
				}
				if (side.getAxis() == Axis.Z) {
					if (this.width <= attached.width) {
						clone.z += (positive ? 0 : -2);
						clone.length += 2;
					}
				}
			}
		}

		return clone;
	}

	public void putAttached(EnumFacing side, Room attached) {
		if (!attachedRooms.containsKey(side))
			attachedRooms.put(side, new HashSet<>());
		attachedRooms.get(side).add(attached);
	}

	public Set<Room> getAttached(EnumFacing side) {
		if (!attachedRooms.containsKey(side))
			return Collections.emptySet();
		return attachedRooms.get(side);
	}

	public Room stack() {
		Room clone = clone();
		clone.attachTo(this, EnumFacing.UP, 0);
		this.roofType = DesignType.NONE;

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			for (Room attached : getAttached(side)) {
				if (!attached.isTop()) {
					for (Room topOfAttached : attached.getAttached(EnumFacing.UP)) {
						if (topOfAttached.y == clone.y) {
							topOfAttached.putAttached(side.getOpposite(), clone);
							clone.putAttached(side, topOfAttached);
						}
					}
				}
			}
		}

		return clone;
	}

	public boolean isTop() {
		return getAttached(EnumFacing.UP).isEmpty();
	}

	public Axis getOrientation() {
		return (width > length) ? Axis.X : Axis.Z;
	}

	public void detach() {
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			for (Room attached : getAttached(side)) {
				attached.getAttached(side.getOpposite()).remove(this);
			}
			attachedRooms.put(side, Collections.emptySet());
		}
	}

	public Room getCuboidAbove() {
		if (getAttached(EnumFacing.UP).size() == 0) {
			return null;
		}
		return new LinkedList<>(getAttached(EnumFacing.UP)).get(0);
	}

}
