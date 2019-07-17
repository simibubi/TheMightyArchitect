package com.simibubi.mightyarchitect;

import com.simibubi.mightyarchitect.block.BlockDesignAnchor;
import com.simibubi.mightyarchitect.block.BlockSliceMarker;
import com.simibubi.mightyarchitect.block.IJustForRendering;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	SLICE_MARKER(new BlockSliceMarker()),
	DESIGN_ANCHOR(new BlockDesignAnchor());
	
	public Block block;

	private AllBlocks(Block block) {
		this.block = block;
		this.block.setRegistryName(TheMightyArchitect.ID, this.name().toLowerCase());
	}

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		for (AllBlocks block : values()) {
			registry.register(block.block);
		}
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		for (AllBlocks block : values()) {
			if (block.get() instanceof IJustForRendering)
				continue;

			registry.register(new BlockItem(block.get(), AllItems.standardProperties())
					.setRegistryName(block.get().getRegistryName()));
		}
	}

	public Block get() {
		return block;
	}

	public boolean typeOf(BlockState state) {
		return state.getBlock() == block;
	}

}
