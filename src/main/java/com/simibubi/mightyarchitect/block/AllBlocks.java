package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetry;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryAxis;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryCrossPlane;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryPlane;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryPoint;
import com.simibubi.mightyarchitect.block.symmetry.BlockSymmetryTriplePlane;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(TheMightyArchitect.ID)
public class AllBlocks {

	public static final BlockSymmetry symmetry_point = new BlockSymmetryPoint("symmetry_point");
	public static final BlockSymmetry symmetry_plane = new BlockSymmetryPlane("symmetry_plane");
	public static final BlockSymmetry symmetry_axis = new BlockSymmetryAxis("symmetry_axis");
	public static final BlockSymmetry symmetry_cross_plane = new BlockSymmetryCrossPlane("symmetry_cross_plane");
	public static final BlockSymmetry symmetry_triple_plane = new BlockSymmetryTriplePlane("symmetry_triple_plane");

	public static final BlockForMightyArchitects slice_marker = new BlockSliceMarker("slice_marker");
	
	private static BlockForMightyArchitects[] placeables = {
			slice_marker
	};
	
	private static BlockForMightyArchitects[] renderOnly = {
			symmetry_point,
			symmetry_plane,
			symmetry_axis,
			symmetry_cross_plane,
			symmetry_triple_plane
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
