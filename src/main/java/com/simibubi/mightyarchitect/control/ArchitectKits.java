package com.simibubi.mightyarchitect.control;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.item.AllItems;
import com.simibubi.mightyarchitect.networking.PacketSender;
import com.simibubi.mightyarchitect.networking.PacketSetHotbarItem;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ArchitectKits {
	
	public static void ExporterToolkit() {
		setHotbarItem(0, AllItems.wand_architect);
		setHotbarItem(1, AllItems.wand_fill);
		setHotbarItem(2, AllItems.wand_symmetry);
		clearHotbarItem(3);
		setHotbarBlock(4, AllBlocks.design_anchor);
		setHotbarBlock(5, AllBlocks.slice_marker);
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
		PacketSender.INSTANCE.sendToServer(new PacketSetHotbarItem(slot, stack));
	}
	
	@SuppressWarnings("deprecation")
	private static void setHotbarBlock(int slot, Block block) {
		setHotbarItem(slot, block.getItem(Minecraft.getMinecraft().world, BlockPos.ORIGIN, block.getDefaultState()));
	}
	
	private static void setHotbarBlock(int slot, Palette palette) {
		IBlockState state = DesignExporter.theme.getDefaultPalette().get(palette);
		@SuppressWarnings("deprecation")
		ItemStack stack = state.getBlock().getItem(Minecraft.getMinecraft().world, BlockPos.ORIGIN, state);
		setHotbarItem(slot, stack.setStackDisplayName("§b" + palette.getDisplayName() + " §f(" + stack.getDisplayName() + ")"));
	}
	
	
}
