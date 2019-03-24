package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryCrossPlane;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryPlane;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryTriplePlane;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(TheMightyArchitect.ID)
public class AllBlocks {

	public static final BlockForMightyArchitects slice_marker = new BlockSliceMarker("slice_marker");
	
	public static final BlockForMightyArchitects symmetry_plane = new BlockSymmetryPlane("symmetry_plane");
	public static final BlockForMightyArchitects symmetry_crossplane = new BlockSymmetryCrossPlane("symmetry_crossplane");
	public static final BlockForMightyArchitects symmetry_tripleplane = new BlockSymmetryTriplePlane("symmetry_tripleplane");
	
	private static BlockForMightyArchitects[] placeables = {
			slice_marker
	};
	
	private static BlockForMightyArchitects[] renderOnly = {
			symmetry_plane, symmetry_crossplane, symmetry_tripleplane
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
