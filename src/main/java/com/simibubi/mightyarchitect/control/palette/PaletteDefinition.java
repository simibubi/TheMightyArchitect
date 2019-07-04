package com.simibubi.mightyarchitect.control.palette;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class PaletteDefinition {

	private Map<Palette, BlockState> definition;
	private String name;
	private BlockState clear;
	private static PaletteDefinition defaultPalette;

	public static PaletteDefinition defaultPalette() {
		if (defaultPalette == null) {
			defaultPalette = new PaletteDefinition("Standard Palette");
			defaultPalette.put(Palette.HEAVY_PRIMARY, Blocks.POLISHED_ANDESITE)
					.put(Palette.HEAVY_SECONDARY, Blocks.COBBLESTONE)
					.put(Palette.HEAVY_WINDOW, Blocks.BLACK_STAINED_GLASS_PANE)
					.put(Palette.HEAVY_POST, Blocks.MOSSY_COBBLESTONE_WALL)
					.put(Palette.INNER_DETAIL, Blocks.SPRUCE_WOOD).put(Palette.INNER_PRIMARY, Blocks.SPRUCE_PLANKS)
					.put(Palette.INNER_SECONDARY, Blocks.DARK_OAK_PLANKS)
					.put(Palette.OUTER_FLAT,
							Blocks.OAK_TRAPDOOR.getDefaultState()
									.with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
									.with(BlockStateProperties.OPEN, true))
					.put(Palette.OUTER_SLAB, Blocks.COBBLESTONE_SLAB).put(Palette.OUTER_THICK, Blocks.COBBLESTONE_WALL)
					.put(Palette.OUTER_THIN, Blocks.SPRUCE_FENCE).put(Palette.ROOF_PRIMARY, Blocks.GRANITE)
					.put(Palette.FLOOR, Blocks.OAK_PLANKS).put(Palette.ROOF_DETAIL, Blocks.BRICKS)
					.put(Palette.CLEAR, Blocks.RED_STAINED_GLASS).put(Palette.ROOF_SLAB, Blocks.BRICK_SLAB)
					.put(Palette.WINDOW, Blocks.GLASS_PANE);
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
		definition.put(Palette.CLEAR, Blocks.RED_STAINED_GLASS.getDefaultState());
		this.name = name;
	}

	public PaletteDefinition put(Palette key, Block block) {
		return put(key, block.getDefaultState());
	}

	public PaletteDefinition put(Palette key, BlockState block) {
		if (block.getBlock() instanceof TrapDoorBlock)
			block = block.with(TrapDoorBlock.OPEN, true);
		definition.put(key, block);
		return this;
	}

	public Map<Palette, BlockState> getDefinition() {
		return definition;
	}

	public BlockState clear() {
		if (clear == null)
			clear = get(Palette.CLEAR);
		return clear;
	}

	public BlockState get(Palette key) {
		BlockState iBlockState = get(key, BlockOrientation.NONE);
		if (iBlockState.getBlock() instanceof LeavesBlock) {
			iBlockState = iBlockState.with(LeavesBlock.PERSISTENT, true);
		}
		return iBlockState;
	}

	private BlockState get(Palette key, BlockOrientation orientation) {
		BlockState iBlockState = definition.get(key);
		return iBlockState == null ? Blocks.AIR.getDefaultState() : orientation.apply(iBlockState);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public CompoundNBT writeToNBT(CompoundNBT compound) {
		compound = (compound == null) ? new CompoundNBT() : compound;
		CompoundNBT palette = new CompoundNBT();
		palette.putString("Name", getName());
		Palette[] values = Palette.values();

		for (int i = 0; i < values.length; i++) {
			CompoundNBT state = NBTUtil.writeBlockState(get(values[i]));
			palette.put(values[i].name(), state);
		}

		compound.put("Palette", palette);
		return compound;
	}

	public static PaletteDefinition fromNBT(CompoundNBT compound) {
		PaletteDefinition palette = defaultPalette().clone();

		if (compound != null) {
			if (compound.contains("Palette")) {
				CompoundNBT paletteTag = compound.getCompound("Palette");
				palette.name = paletteTag.getString("Name");
				for (Palette key : Palette.values()) {
					if (paletteTag.contains(key.name())) {
						palette.put(key, NBTUtil.readBlockState(paletteTag.getCompound(key.name())));
					}
				}
			}
		}
		return palette;
	}

	public BlockState get(PaletteBlockInfo paletteInfo) {
		BlockState state = definition.get(paletteInfo.palette);
		state = state == null ? Blocks.AIR.getDefaultState() : paletteInfo.apply(state);

		Collection<IProperty<?>> properties = state.getProperties();

		for (IProperty<?> property : properties) {
			if (property instanceof DirectionProperty) {
				Direction facing = (Direction) state.get(property);
				if (facing.getAxis() == Axis.Y)
					continue;

				if ((paletteInfo.mirrorZ && facing.getAxis() != Axis.Z)
						|| (paletteInfo.mirrorX && facing.getAxis() != Axis.X))
					state = state.with((DirectionProperty) property, facing.getOpposite());
			}
		}

		return state;
	}

	public String getDuplicates() {
		for (Palette key : definition.keySet()) {
			Palette keyIgnoreRotation = getKeyIgnoreRotation(definition.get(key));
			if (key != keyIgnoreRotation) {
				return key.getDisplayName() + " = " + keyIgnoreRotation.getDisplayName();
			}
		}
		return "";
	}

	public boolean hasDuplicates() {
		for (Palette key : definition.keySet()) {
			if (key != getKeyIgnoreRotation(definition.get(key))) {
				return true;
			}
		}
		return false;
	}

	public Palette scan(BlockState state) {
		if (state.getBlock() == Blocks.AIR)
			return null;

		if (definition.containsValue(state)) {
			for (Palette key : definition.keySet())
				if (definition.get(key).equals(state))
					return key;
		}

		// contains but rotated
		Palette keyIgnoreRotation = getKeyIgnoreRotation(state);
		return keyIgnoreRotation;
	}

	protected Palette getKeyIgnoreRotation(BlockState state) {
		Map<Block, Palette> scanMap = new HashMap<>();
		definition.forEach((palette, block) -> {
			scanMap.put(block.getBlock(), palette);
		});
		
		if (scanMap.containsKey(state.getBlock()))
			return scanMap.get(state.getBlock());
		
		return null;
	}

}
