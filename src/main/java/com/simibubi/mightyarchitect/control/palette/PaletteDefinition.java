package com.simibubi.mightyarchitect.control.palette;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.BlockTrapDoor.DoorHalf;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class PaletteDefinition {

	private Map<Palette, IBlockState> definition;
	private String name;
	private IBlockState clear;
	private static PaletteDefinition defaultPalette;

	public static PaletteDefinition defaultPalette() {
		if (defaultPalette == null) {
			defaultPalette = new PaletteDefinition("Standard Palette");
			defaultPalette
					.put(Palette.HEAVY_PRIMARY,
							Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
									BlockStone.EnumType.ANDESITE_SMOOTH))
					.put(Palette.HEAVY_SECONDARY, Blocks.COBBLESTONE.getDefaultState())
					.put(Palette.HEAVY_WINDOW,
							Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR,
									EnumDyeColor.BLACK))
					.put(Palette.HEAVY_POST,
							Blocks.COBBLESTONE_WALL.getDefaultState().withProperty(BlockWall.VARIANT,
									BlockWall.EnumType.MOSSY))
					.put(Palette.INNER_DETAIL,
							Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, EnumType.SPRUCE))
					.put(Palette.INNER_PRIMARY,
							Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, EnumType.SPRUCE))
					.put(Palette.INNER_SECONDARY,
							Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, EnumType.DARK_OAK))
					.put(Palette.OUTER_FLAT,
							Blocks.TRAPDOOR.getDefaultState().withProperty(BlockTrapDoor.FACING, EnumFacing.SOUTH)
									.withProperty(BlockTrapDoor.OPEN, true))
					.put(Palette.OUTER_SLAB,
							Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT,
									BlockStoneSlab.EnumType.COBBLESTONE))
					.put(Palette.OUTER_THICK, Blocks.COBBLESTONE_WALL.getDefaultState())
					.put(Palette.OUTER_THIN, Blocks.SPRUCE_FENCE.getDefaultState())
					.put(Palette.ROOF_PRIMARY,
							Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
									BlockStone.EnumType.GRANITE))
					.put(Palette.FLOOR, Blocks.PLANKS.getDefaultState())
					.put(Palette.ROOF_DETAIL, Blocks.BRICK_BLOCK.getDefaultState())
					.put(Palette.CLEAR, Blocks.GLASS.getDefaultState())
					.put(Palette.ROOF_SLAB,
							Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT,
									BlockStoneSlab.EnumType.BRICK))
					.put(Palette.WINDOW, Blocks.GLASS_PANE.getDefaultState());
		}
		return defaultPalette;
	}

	public PaletteDefinition clone() {
		PaletteDefinition clone = new PaletteDefinition(name);
		clone.clear = defaultPalette().clear();
		clone.definition = new HashMap<>(defaultPalette().getDefinition());
		definition.forEach((key, value) -> clone.definition.put(key, value));
		return clone;
	}

	public PaletteDefinition(String name) {
		definition = new HashMap<>();
		definition.put(Palette.CLEAR, Blocks.GLASS.getDefaultState());
		this.name = name;
	}

	public PaletteDefinition put(Palette key, IBlockState block) {
		if (block.getBlock() instanceof BlockTrapDoor)
			block = block.withProperty(BlockTrapDoor.OPEN, true);
		definition.put(key, block);
		return this;
	}

	public Map<Palette, IBlockState> getDefinition() {
		return definition;
	}

	public IBlockState clear() {
		if (clear == null)
			clear = get(Palette.CLEAR);
		return clear;
	}

	public IBlockState get(Palette key) {
		return get(key, BlockOrientation.NONE);
	}

	private IBlockState get(Palette key, BlockOrientation orientation) {
		IBlockState iBlockState = definition.get(key);
		return iBlockState == null ? Blocks.AIR.getDefaultState() : orientation.apply(iBlockState);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = (compound == null) ? new NBTTagCompound() : compound;
		NBTTagCompound palette = new NBTTagCompound();
		palette.setString("Name", getName());
		Palette[] values = Palette.values();

		for (int i = 0; i < values.length; i++) {
			NBTTagCompound state = new NBTTagCompound();
			NBTUtil.writeBlockState(state, get(values[i]));
			palette.setTag(values[i].name(), state);
		}

		compound.setTag("Palette", palette);
		return compound;
	}

	public static PaletteDefinition fromNBT(NBTTagCompound compound) {
		PaletteDefinition palette = defaultPalette().clone();

		if (compound != null) {
			if (compound.hasKey("Palette")) {
				NBTTagCompound paletteTag = compound.getCompoundTag("Palette");
				palette.name = paletteTag.getString("Name");
				for (Palette key : Palette.values()) {
					if (paletteTag.hasKey(key.name())) {
						palette.put(key, NBTUtil.readBlockState(paletteTag.getCompoundTag(key.name())));
					}
				}
			}
		}
		return palette;
	}

	public IBlockState get(PaletteBlockInfo paletteInfo) {
		IBlockState state = definition.get(paletteInfo.palette);
		state = state == null ? Blocks.AIR.getDefaultState()
				: paletteInfo.orientation.apply(state, paletteInfo.forceAxis);

		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();

		for (IProperty<?> property : properties.keySet()) {
			if (property instanceof PropertyDirection) {
				EnumFacing facing = (EnumFacing) properties.get(property);
				if (facing.getAxis() == Axis.Y)
					continue;

				if ((paletteInfo.mirrorZ && facing.getAxis() != Axis.Z)
						|| (paletteInfo.mirrorX && facing.getAxis() != Axis.X))
					state = state.withProperty((PropertyDirection) property, facing.getOpposite());
			}
		}

		return state;
	}

	public boolean hasDuplicates() {
		for (Palette key : definition.keySet()) {
			if (key != getKeyIgnoreRotation(definition.get(key))) {
				return true;
			}
		}
		return false;
	}

	public Palette scan(IBlockState state) {
		if (state.getBlock() == Blocks.AIR)
			return null;

		if (definition.containsValue(state)) {
			for (Palette key : definition.keySet())
				if (definition.get(key).equals(state))
					return key;
		}

		// contains but rotated
		return getKeyIgnoreRotation(state);
	}

	protected Palette getKeyIgnoreRotation(IBlockState state) {
		Map<IBlockState, Palette> scanMap = new HashMap<>();
		definition.forEach((palette, block) -> {
			scanMap.put(block, palette);
		});
		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
		for (IProperty<?> property : properties.keySet()) {

			// I'm so sorry
			if (property == BlockSlab.HALF)
				for (EnumBlockHalf half : EnumBlockHalf.values())
					if (scanMap.containsKey(state.withProperty(BlockSlab.HALF, half)))
						return scanMap.get(state.withProperty(BlockSlab.HALF, half));

			if (property == BlockStairs.HALF)
				for (EnumHalf half : EnumHalf.values())
					if (scanMap.containsKey(state.withProperty(BlockStairs.HALF, half)))
						return scanMap.get(state.withProperty(BlockStairs.HALF, half));

			if (property == BlockTrapDoor.HALF)
				for (DoorHalf half : DoorHalf.values())
					if (scanMap.containsKey(state.withProperty(BlockTrapDoor.HALF, half)))
						return scanMap.get(state.withProperty(BlockTrapDoor.HALF, half));

			if (property == BlockRotatedPillar.AXIS)
				for (Axis axis : Axis.values())
					if (scanMap.containsKey(state.withProperty(BlockRotatedPillar.AXIS, axis)))
						return scanMap.get(state.withProperty(BlockRotatedPillar.AXIS, axis));

			if (property == BlockLog.LOG_AXIS)
				for (EnumAxis axis : EnumAxis.values())
					if (scanMap.containsKey(state.withProperty(BlockLog.LOG_AXIS, axis)))
						return scanMap.get(state.withProperty(BlockLog.LOG_AXIS, axis));

			if (property instanceof PropertyDirection)
				for (Comparable<?> facing : property.getAllowedValues())
					if (scanMap.containsKey(state.withProperty((PropertyDirection) property, (EnumFacing) facing)))
						return scanMap.get(state.withProperty((PropertyDirection) property, (EnumFacing) facing));
		}

		if (definition.containsValue(state)) {
			for (Palette key : definition.keySet())
				if (definition.get(key).equals(state))
					return key;
		}
		return null;
	}

}
