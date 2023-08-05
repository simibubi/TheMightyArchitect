package com.simibubi.mightyarchitect.foundation.utility;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class BlockHelper {

	@SuppressWarnings("deprecation")
	public static HolderGetter<Block> lookup() {
		return BuiltInRegistries.BLOCK.asLookup();
	}
	
}
