package com.simibubi.mightyarchitect.control;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.AllItems;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.networking.PacketSetHotbarItem;
import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class ArchitectKits {

	public static void ExporterToolkit() {
		setHotbarItem(0, AllItems.ARCHITECT_WAND.get());
		setHotbarItem(1, AllItems.FILLING_WAND.get());
		setHotbarItem(2, AllItems.SYMMETRY_WAND.get());
		clearHotbarItem(3);
		setHotbarBlock(4, AllBlocks.DESIGN_ANCHOR.get());
		setHotbarBlock(5, AllBlocks.SLICE_MARKER.get());
		clearHotbarItem(6);
		setHotbarBlock(7, Palette.CLEAR);
		setHotbarBlock(8, Palette.FLOOR);
	}

	public static void FoundationToolkit() {
		setHotbarBlock(0, Palette.HEAVY_POST);
		setHotbarBlock(1, Palette.HEAVY_PRIMARY);
		setHotbarBlock(2, Palette.HEAVY_SECONDARY);
		setHotbarBlock(3, Palette.HEAVY_WINDOW);
		setHotbarBlock(4, Palette.INNER_DETAIL);
		setHotbarBlock(5, Palette.INNER_PRIMARY);
		setHotbarBlock(6, Palette.OUTER_SLAB);
		setHotbarBlock(7, Palette.OUTER_THICK);
		setHotbarBlock(8, Palette.OUTER_THIN);
	}

	public static void RegularToolkit() {
		setHotbarBlock(0, Palette.FLOOR);
		setHotbarBlock(1, Palette.INNER_PRIMARY);
		setHotbarBlock(2, Palette.INNER_DETAIL);
		setHotbarBlock(3, Palette.INNER_SECONDARY);
		setHotbarBlock(4, Palette.OUTER_FLAT);
		setHotbarBlock(5, Palette.WINDOW);
		setHotbarBlock(6, Palette.OUTER_SLAB);
		setHotbarBlock(7, Palette.OUTER_THICK);
		setHotbarBlock(8, Palette.OUTER_THIN);
	}

	public static void RoofingToolkit() {
		setHotbarBlock(0, Palette.ROOF_PRIMARY);
		setHotbarBlock(1, Palette.ROOF_DETAIL);
		setHotbarBlock(2, Palette.ROOF_SLAB);
		setHotbarBlock(3, Palette.WINDOW);
		setHotbarBlock(4, Palette.INNER_PRIMARY);
		setHotbarBlock(5, Palette.INNER_DETAIL);
		setHotbarBlock(6, Palette.INNER_SECONDARY);
		setHotbarBlock(7, Palette.OUTER_THICK);
		setHotbarBlock(8, Palette.OUTER_THIN);
	}

	private static void clearHotbarItem(int slot) {
		setHotbarItem(slot, ItemStack.EMPTY);
	}

	private static void setHotbarItem(int slot, Item item) {
		setHotbarItem(slot, new ItemStack(item));
	}

	private static void setHotbarItem(int slot, ItemStack stack) {
		Packets.channel.sendToServer(new PacketSetHotbarItem(slot, stack));
	}

	private static void setHotbarBlock(int slot, Block block) {
		setHotbarItem(slot, block.asItem());
	}

	private static void setHotbarBlock(int slot, Palette palette) {
		BlockState state = DesignExporter.theme.getDefaultPalette().get(palette);
		ItemStack stack = new ItemStack(state.getBlock().asItem());
		setHotbarItem(slot, stack.setDisplayName(
				new StringTextComponent(palette.getDisplayName() + " (" + stack.getDisplayName() + ")")));
	}

}
