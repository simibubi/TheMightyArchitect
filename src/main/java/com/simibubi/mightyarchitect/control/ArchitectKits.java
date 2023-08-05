package com.simibubi.mightyarchitect.control;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.AllItems;
import com.simibubi.mightyarchitect.AllPackets;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.networking.SetHotbarItemPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ArchitectKits {

	public static void ExporterToolkit() {
		setHotbarItem(0, AllItems.ARCHITECT_WAND.get());
		setHotbarBlock(1, AllBlocks.DESIGN_ANCHOR.get());
		setHotbarBlock(2, AllBlocks.SLICE_MARKER.get());
		clearHotbarItem(3);
		setHotbarBlock(4, Palette.CLEAR);
		setHotbarBlock(5, Palette.FLOOR);
		clearHotbarItem(6);
		clearHotbarItem(7);
		clearHotbarItem(8);
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
		AllPackets.channel.sendToServer(new SetHotbarItemPacket(slot, stack));
	}

	private static void setHotbarBlock(int slot, Block block) {
		setHotbarItem(slot, block.asItem());
	}

	private static void setHotbarBlock(int slot, Palette palette) {
		BlockState state = DesignExporter.theme.getDefaultPalette()
			.get(palette);
		ItemStack stack = new ItemStack(state.getBlock()
			.asItem());
		setHotbarItem(slot,
			stack.setHoverName(Lang
				.text(ChatFormatting.RESET + "" + ChatFormatting.GOLD + palette.getDisplayName() + ChatFormatting.WHITE
					+ " (" + ChatFormatting.GRAY + stack.getHoverName()
						.getString()
					+ ChatFormatting.WHITE + ")")
				.component()));
	}

}
