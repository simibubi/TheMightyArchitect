package com.simibubi.mightyarchitect.item.symmetry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class SymmetryElement {

	public static final String EMPTY = "empty";
	public static final String PLANE = "plane";
	public static final String CROSS_PLANE = "cross_plane";
	public static final String TRIPLE_PLANE = "triple_plane";

	public static final List<String> TOOLTIP_ELEMENTS = ImmutableList.of("Mirror once", "Rectanglar", "Octagonal");

	protected Vec3d position;
	protected IStringSerializable orientation;
	protected int orientationIndex;
	public boolean enable;

	public SymmetryElement(Vec3d pos) {
		position = pos;
		enable = true;
		orientationIndex = 0;
	}

	public IStringSerializable getOrientation() {
		return orientation;
	}

	public Vec3d getPosition() {
		return position;
	}

	public int getOrientationIndex() {
		return orientationIndex;
	}

	public void rotate(boolean forward) {
		orientationIndex += forward ? 1 : -1;
		setOrientation();
	}

	public void process(Map<BlockPos, IBlockState> blocks) {
		Map<BlockPos, IBlockState> result = new HashMap<>();
		for (BlockPos pos : blocks.keySet()) {
			result.putAll(process(pos, blocks.get(pos)));
		}
		blocks.putAll(result);
	}

	public abstract Map<BlockPos, IBlockState> process(BlockPos position, IBlockState block);

	protected abstract void setOrientation();

	public abstract void setOrientation(int index);

	public abstract String typeName();

	public abstract IBlockState getModel();

	private static final String $ORIENTATION = "direction";
	private static final String $POSITION = "pos";
	private static final String $TYPE = "type";
	private static final String $ENABLE = "enable";

	public NBTTagCompound writeToNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger($ORIENTATION, orientationIndex);

		NBTTagList floatList = new NBTTagList();
		floatList.appendTag(new NBTTagFloat((float) position.x));
		floatList.appendTag(new NBTTagFloat((float) position.y));
		floatList.appendTag(new NBTTagFloat((float) position.z));
		nbt.setTag($POSITION, floatList);
		nbt.setString($TYPE, typeName());
		nbt.setBoolean($ENABLE, enable);

		return nbt;
	}

	public static SymmetryElement fromNBT(NBTTagCompound nbt) {
		NBTTagList floatList = nbt.getTagList($POSITION, 5);
		Vec3d pos = new Vec3d(floatList.getFloatAt(0), floatList.getFloatAt(1), floatList.getFloatAt(2));
		SymmetryElement element;

		switch (nbt.getString($TYPE)) {
		case PLANE:
			element = new SymmetryPlane(pos);
			break;
		case CROSS_PLANE:
			element = new SymmetryCrossPlane(pos);
			break;
		case TRIPLE_PLANE:
			element = new SymmetryTriplePlane(pos);
			break;
		default:
			element = new SymmetryEmptySlot(pos);
			break;
		}

		element.setOrientation(nbt.getInteger($ORIENTATION));
		element.enable = nbt.getBoolean($ENABLE);

		return element;
	}

	protected Vec3d getDiff(BlockPos position) {
		return this.position.scale(-1).addVector(position.getX(), position.getY(), position.getZ());
	}

	protected BlockPos getIDiff(BlockPos position) {
		Vec3d diff = getDiff(position);
		return new BlockPos((int) diff.x, (int) diff.y, (int) diff.z);
	}

	protected IBlockState flipX(IBlockState in) {
		return in.withMirror(Mirror.FRONT_BACK);
	}

	protected IBlockState flipY(IBlockState in) {
		for (IProperty<?> property : in.getPropertyKeys()) {

			// Stairs
			if (property.getValueClass().equals(EnumHalf.class)) {
				return in.cycleProperty(property);

				// Slabs
			} else if (property.getValueClass().equals(EnumBlockHalf.class)) {
				return in.cycleProperty(property);

				// Directional Blocks
			} else if (property instanceof PropertyDirection) {
				if (in.getProperties().get(property) == EnumFacing.DOWN) {
					return in.withProperty((PropertyDirection) property, EnumFacing.UP);
				} else if (in.getProperties().get(property) == EnumFacing.UP) {
					return in.withProperty((PropertyDirection) property, EnumFacing.DOWN);
				}
			}
		}
		return in;
	}

	protected IBlockState flipZ(IBlockState in) {
		return in.withMirror(Mirror.LEFT_RIGHT);
	}

	protected IBlockState flipD1(IBlockState in) {
		for (IProperty<?> property : in.getPropertyKeys()) {

			if (property instanceof PropertyEnum<?>) {
				if (property == BlockRotatedPillar.AXIS) {
					Axis axis = ((Axis) in.getProperties().get(property));
					if (axis.isVertical())
						return in;
					return in.withProperty(BlockRotatedPillar.AXIS, (axis == Axis.X ? Axis.Z : Axis.X));
				} 
				if (property == BlockLog.LOG_AXIS) {
					EnumAxis axis = ((EnumAxis) in.getProperties().get(property));
					if (axis == EnumAxis.Y || axis == EnumAxis.NONE)
						return in;
					return in.withProperty(BlockLog.LOG_AXIS, (axis == EnumAxis.X ? EnumAxis.Z : EnumAxis.X));					
				}
			}
			
			if (property instanceof PropertyDirection) {
				switch ((EnumFacing) in.getProperties().get(property)) {
				case EAST:
					return in.withProperty((PropertyDirection) property, EnumFacing.NORTH);
				case NORTH:
					return in.withProperty((PropertyDirection) property, EnumFacing.EAST);
				case SOUTH:
					return in.withProperty((PropertyDirection) property, EnumFacing.WEST);
				case WEST:
					return in.withProperty((PropertyDirection) property, EnumFacing.SOUTH);
				default:
					break;
				}
			}

		}
		return in;
	}

	protected IBlockState flipD2(IBlockState in) {
		for (IProperty<?> property : in.getPropertyKeys()) {

			if (property instanceof PropertyEnum<?>) {
				if (property == BlockRotatedPillar.AXIS) {
					Axis axis = ((Axis) in.getProperties().get(property));
					if (axis.isVertical())
						return in;
					return in.withProperty(BlockRotatedPillar.AXIS, (axis == Axis.X ? Axis.Z : Axis.X));
				} 
				if (property == BlockLog.LOG_AXIS) {
					EnumAxis axis = ((EnumAxis) in.getProperties().get(property));
					if (axis == EnumAxis.Y || axis == EnumAxis.NONE)
						return in;
					return in.withProperty(BlockLog.LOG_AXIS, (axis == EnumAxis.X ? EnumAxis.Z : EnumAxis.X));					
				}
			}
			
			if (property instanceof PropertyDirection) {
				switch ((EnumFacing) in.getProperties().get(property)) {
				case EAST:
					return in.withProperty((PropertyDirection) property, EnumFacing.SOUTH);
				case NORTH:
					return in.withProperty((PropertyDirection) property, EnumFacing.WEST);
				case SOUTH:
					return in.withProperty((PropertyDirection) property, EnumFacing.EAST);
				case WEST:
					return in.withProperty((PropertyDirection) property, EnumFacing.NORTH);
				default:
					break;
				}
			}

		}
		return in;
	}

	protected BlockPos flipX(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - 2 * diff.getX(), position.getY(), position.getZ());
	}

	protected BlockPos flipY(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY() - 2 * diff.getY(), position.getZ());
	}

	protected BlockPos flipZ(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY(), position.getZ() - 2 * diff.getZ());
	}

	protected BlockPos flipD2(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() + diff.getZ(), position.getY(),
				position.getZ() - diff.getZ() + diff.getX());
	}

	protected BlockPos flipD1(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() - diff.getZ(), position.getY(),
				position.getZ() - diff.getZ() - diff.getX());
	}

	public void setPosition(Vec3d pos3d) {
		this.position = pos3d;
	}
	
	public abstract List<String> getAlignToolTips();

}
