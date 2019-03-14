package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class Cuboid {

	public int x, y, z, width, height, length;
	public DesignLayer designLayer;
	public DesignType roofType;
	public char styleGroup;
	public int layer;
	private boolean isSecondary;

	private Map<EnumFacing, Set<Cuboid>> attachedCuboids;

	public Cuboid(BlockPos origin, BlockPos size) {
		this(origin, size.getX(), size.getY(), size.getZ());
	}

	public Cuboid(BlockPos origin, int width, int height, int length) {
		attachedCuboids = new HashMap<>();
		this.x = origin.getX() + ((width < 0) ? width : 0);
		this.y = origin.getY() + ((height < 0) ? height : 0);
		this.z = origin.getZ() + ((length < 0) ? length : 0);
		this.width = Math.abs(width);
		this.height = Math.abs(height);
		this.length = Math.abs(length);
		styleGroup = 'A';
		designLayer = DesignLayer.None;
		roofType = DesignType.ROOF;
	}

	public BlockPos getOrigin() {
		return new BlockPos(x, y, z);
	}

	public BlockPos getSize() {
		return new BlockPos(width, height, length);
	}

	public Cuboid clone() {
		return new Cuboid(new BlockPos(x, y, z), width, height, length);
	}

	public void move(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void centerHorizontallyOn(BlockPos pos) {
		x = pos.getX() - (width / 2);
		y = pos.getY();
		z = pos.getZ() - (length / 2);
	}

	public boolean intersects(Cuboid other) {
		return !(other.x >= x + width || other.z >= z + length || other.x + other.width <= x
				|| other.z + other.length <= z);
	}

	public void attachTo(Cuboid other, EnumFacing side, int shift) {
		moveToAttach(other, side, shift);
		other.putAttached(side, this);
		putAttached(side.getOpposite(), other);

	}

	public void moveToAttach(Cuboid other, EnumFacing side, int shift) {
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

	private void centerOnOthersZ(Cuboid other, int shift) {
		this.z = other.z + shift + (other.length - this.length) / 2;
	}

	private void centerOnOthersX(Cuboid other, int shift) {
		this.x = other.x + shift + (other.width - this.width) / 2;
	}

	public Cuboid getClearing() {
		Cuboid clone = clone();
		clone.x += 1;
		clone.z += 1;
		clone.width -= 2;
		clone.length -= 2;

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			for (Cuboid attached : getAttached(side)) {
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

	public void putAttached(EnumFacing side, Cuboid attached) {
		if (!attachedCuboids.containsKey(side))
			attachedCuboids.put(side, new HashSet<>());
		attachedCuboids.get(side).add(attached);
	}

	public Set<Cuboid> getAttached(EnumFacing side) {
		if (!attachedCuboids.containsKey(side))
			return Collections.emptySet();
		return attachedCuboids.get(side);
	}

	public Cuboid stack() {
		Cuboid clone = clone();
		clone.styleGroup = styleGroup;
		clone.isSecondary = isSecondary;
		clone.attachTo(this, EnumFacing.UP, 0);
		
		clone.roofType = roofType;
		this.roofType = DesignType.NONE;

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			for (Cuboid attached : getAttached(side)) {
				if (!attached.isTop()) {
					for (Cuboid topOfAttached : attached.getAttached(EnumFacing.UP)) {
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
			for (Cuboid attached : getAttached(side)) {
				attached.getAttached(side.getOpposite()).remove(this);
			}
			attachedCuboids.put(side, Collections.emptySet());
		}
	}

	public NBTTagCompound asNBTCompound() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Origin", NBTUtil.createPosTag(getOrigin()));
		compound.setTag("Size", NBTUtil.createPosTag(getSize()));
		compound.setString("DesignLayer", designLayer.name());
		compound.setInteger("StyleGroup", styleGroup);
		return compound;
	}

	public static Cuboid readFromNBT(NBTTagCompound compound) {
		BlockPos origin = NBTUtil.getPosFromTag(compound.getCompoundTag("Origin"));
		BlockPos size = NBTUtil.getPosFromTag(compound.getCompoundTag("Size"));
		Cuboid cuboid = new Cuboid(origin, size);
		cuboid.designLayer = DesignLayer.valueOf(compound.getString("DesignLayer"));
		cuboid.styleGroup = (char) compound.getInteger("StyleGroup");
		return cuboid;
	}

	public boolean contains(BlockPos pos) {
		return (pos.getX() >= x && pos.getX() < x + width) && (pos.getY() >= y && pos.getY() < y + height)
				&& (pos.getZ() >= z && pos.getZ() < z + length);
	}
	
	public BlockPos getCenter() {
		return getOrigin().add(width / 2, height / 2, length / 2);
	}
	
	public Cuboid getCuboidAbove() {
		if (getAttached(EnumFacing.UP).size() > 0) {
			return new LinkedList<>(getAttached(EnumFacing.UP)).get(0);
		}
		return null;
	}

	public boolean isSecondary() {
		return isSecondary;
	}

	public void setSecondary(boolean isSecondary) {
		this.isSecondary = isSecondary;
	}

}
