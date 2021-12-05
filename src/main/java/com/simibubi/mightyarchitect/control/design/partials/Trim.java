package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundTag;

public class Trim extends Design {

	@Override
	public Design fromNBT(CompoundTag compound) {
		Trim trim = new Trim();
		trim.applyNBT(compound);
		return trim;
	}

	
}
