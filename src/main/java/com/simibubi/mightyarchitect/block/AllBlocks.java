package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(TheMightyArchitect.ID)
public class AllBlocks {

	public static final BlockForMightyArchitects slice_marker = new BlockSliceMarker("slice_marker");
	
	private static BlockForMightyArchitects[] placeables = {
			slice_marker
	};
	
	private static BlockForMightyArchitects[] renderOnly = {
	};
	
	public static void registerAll(IForgeRegistry<Block> registry) {
		registry.registerAll(placeables);
		registry.registerAll(renderOnly);
	}
	
	public static void registerAllItemBlocks(IForgeRegistry<Item> registry) {
		for (Block block : placeables)
			registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	public static void initModels() {
		for (BlockForMightyArchitects block : placeables)
			block.initItemModel();
	}

}
