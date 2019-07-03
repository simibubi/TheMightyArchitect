package com.simibubi.mightyarchitect;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class MightyArchitectItemGroup extends ItemGroup {

	public MightyArchitectItemGroup() {
		super(getGroupCountSafe(), TheMightyArchitect.ID);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllItems.BLUEPRINT.get());
	}
}
