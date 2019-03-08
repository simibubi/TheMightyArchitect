package com.simibubi.mightyarchitect.buildomatico;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

public class PaletteDefinition {

	private Map<Palette, IBlockState> definition;
	private String name;
	private IBlockState clear;
	private static PaletteDefinition defaultPalette;

	public static PaletteDefinition defaultPalette() {
		if (defaultPalette == null) {
			defaultPalette = new PaletteDefinition("Default Palette");
			defaultPalette
					.put(Palette.HEAVY_PRIMARY,
							Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
									BlockStone.EnumType.ANDESITE_SMOOTH))
					.put(Palette.HEAVY_SECONDARY, Blocks.COBBLESTONE.getDefaultState())
					.put(Palette.HEAVY_WINDOW,
							Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR,
									EnumDyeColor.BLACK))
					.put(Palette.HEAVY_POST, Blocks.COBBLESTONE_WALL.getDefaultState().withProperty(BlockWall.VARIANT, BlockWall.EnumType.MOSSY))
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
					.put(Palette.ROOF_SECONDARY,
							Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
									BlockStone.EnumType.GRANITE_SMOOTH))
					.put(Palette.ROOF_DETAIL, Blocks.BRICK_BLOCK.getDefaultState())
					.put(Palette.CLEAR, Blocks.GLASS.getDefaultState())
					.put(Palette.ROOF_SLAB, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT,
							BlockStoneSlab.EnumType.BRICK))
					.put(Palette.ROOF_SLAB_TOP, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT,
							BlockStoneSlab.EnumType.BRICK).withProperty(BlockSlab.HALF, EnumBlockHalf.TOP))
					.put(Palette.WINDOW, Blocks.GLASS_PANE.getDefaultState());
		}
		return defaultPalette;
	}
	
	public PaletteDefinition clone() {
		PaletteDefinition clone = new PaletteDefinition(name);
		clone.clear = defaultPalette().clear();
		clone.definition = new HashMap<>(defaultPalette().getDefinition());
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

	@Deprecated
	public IBlockState get(Palette key) {
		if (key == Palette.ROOF_SLAB_TOP) {
			IBlockState roofSlab = get(Palette.ROOF_SLAB);
			if (roofSlab.getPropertyKeys().contains(BlockSlab.HALF))
				return roofSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
			return roofSlab;
		}
		return definition.get(key);
	}
	
	public IBlockState get(Palette key, EnumFacing facing) {
		IBlockState iBlockState = definition.get(key);		
		if (key == Palette.ROOF_SLAB_TOP) {
			IBlockState roofSlab = get(Palette.ROOF_SLAB, facing);
			if (roofSlab.getPropertyKeys().contains(BlockSlab.HALF))
				return roofSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
			return roofSlab;
		}
		return iBlockState.withRotation(rotationFromFacing(facing));
	}
	
	private Rotation rotationFromFacing(EnumFacing facing) {
		switch (facing) {
		case EAST:
			return Rotation.COUNTERCLOCKWISE_90;
		case WEST:
			return Rotation.CLOCKWISE_90;
		case NORTH:
			return Rotation.CLOCKWISE_180;
		case SOUTH:
		default:
			return Rotation.NONE;
		}
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
	
	public String getName() {
		return name;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = (compound == null)? new NBTTagCompound() : compound;
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
	
	public void setName(String name) {
		this.name = name;
	}
	

}
