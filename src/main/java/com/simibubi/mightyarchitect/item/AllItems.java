package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(TheMightyArchitect.ID)
public class AllItems {

	public static final ItemForMightyArchitects wand_architect = new ItemWandArchitect("wand_architect");
	public static final ItemForMightyArchitects wand_symmetry = new ItemWandSymmetry("wand_symmetry");
	public static final ItemForMightyArchitects wand_fill = new ItemWandFill("wand_fill");
	
	public static final ItemForMightyArchitects blueprint_empty = new ItemBlueprintEmpty("blueprint_empty");
	public static final ItemForMightyArchitects blueprint_filled = new ItemBlueprintFilled("blueprint_filled");

	public static void registerAll(IForgeRegistry<Item> registry) {
		registry.registerAll(wand_architect, wand_symmetry, wand_fill, blueprint_filled);
	}
	
	public static void initModels() {
		wand_fill.initModel();
		wand_architect.initModel();
		wand_symmetry.initModel();
		blueprint_empty.initModel();
		blueprint_filled.initModel();
	}
	
	public static void initColorHandlers() {
		
	}
	
}
