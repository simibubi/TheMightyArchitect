package com.simibubi.mightyarchitect.item;

import net.minecraft.item.ItemStack;

public class ItemWandArchitect extends ItemForMightyArchitects {

	public ItemWandArchitect(String name) {
		super(name);
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

}
