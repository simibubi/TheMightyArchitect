package com.simibubi.mightyarchitect;

import com.simibubi.mightyarchitect.block.DesignAnchorBlock;
import com.simibubi.mightyarchitect.block.IJustForRendering;
import com.simibubi.mightyarchitect.block.SliceMarkerBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	SLICE_MARKER(new SliceMarkerBlock()),
	DESIGN_ANCHOR(new DesignAnchorBlock());
	
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
