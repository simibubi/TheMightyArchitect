package com.simibubi.mightyarchitect;

import com.simibubi.mightyarchitect.item.ItemWandArchitect;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllItems {
	
	ARCHITECT_WAND(new ItemWandArchitect(standardProperties())),
	SYMMETRY_WAND(new ItemWandArchitect(standardProperties())),
	FILLING_WAND(new ItemWandArchitect(standardProperties())),
	
	EMPTY_BLUEPRINT(new Item(standardProperties().maxStackSize(1))),
	BLUEPRINT(new Item(standardProperties().maxStackSize(1)));

	public Item item;

	private AllItems(Item item) {
		this.item = item;
		this.item.setRegistryName(TheMightyArchitect.ID, this.name().toLowerCase().replace("_", ""));
	}
	
	public static Properties standardProperties() {
		return new Properties().group(TheMightyArchitect.creativeTab);
	}
	
	public static void registerItems(IForgeRegistry<Item> iForgeRegistry) {
		for (AllItems item : values()) {
			iForgeRegistry.register(item.get());
		}
	}
	
	public Item get() {
		return item;
	}
	
	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	public static void initColorHandlers() {
	}
	
}
