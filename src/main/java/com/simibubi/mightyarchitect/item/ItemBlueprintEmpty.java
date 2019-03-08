package com.simibubi.mightyarchitect.item;

import net.minecraft.item.ItemStack;

public class ItemBlueprintEmpty extends ItemForMightyArchitects {

	public ItemBlueprintEmpty(String name) {
		super(name);
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}
	
}
