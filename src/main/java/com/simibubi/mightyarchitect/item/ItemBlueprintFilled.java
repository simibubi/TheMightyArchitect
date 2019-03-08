package com.simibubi.mightyarchitect.item;

import net.minecraft.item.ItemStack;

public class ItemBlueprintFilled extends ItemForMightyArchitects {

	public ItemBlueprintFilled(String name) {
		super(name);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

}
