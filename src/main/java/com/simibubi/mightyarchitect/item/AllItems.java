package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(TheMightyArchitect.ID)
public class AllItems {

	public static final ItemForMightyArchitects wand_symmetry = new ItemWandSymmetry("wand_symmetry");
	public static final ItemForMightyArchitects wand_architect = new ItemWandArchitect("wand_architect");
	public static final ItemForMightyArchitects blueprint_empty = new ItemBlueprintEmpty("blueprint_empty");
	public static final ItemForMightyArchitects blueprint_filled = new ItemBlueprintFilled("blueprint_filled");

	public static void registerAll(IForgeRegistry<Item> registry) {
		registry.registerAll(wand_architect, blueprint_empty, wand_symmetry, blueprint_filled);
	}
	
	public static void initModels() {
		wand_architect.initModel();
		blueprint_empty.initModel();
		blueprint_filled.initModel();
		wand_symmetry.initModel();
	}
	
	public static void initColorHandlers() {
		
	}
	
}
