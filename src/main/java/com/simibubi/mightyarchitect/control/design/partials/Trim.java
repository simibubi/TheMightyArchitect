package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundNBT;

public class Trim extends Design {

	@Override
	public Design fromNBT(CompoundNBT compound) {
		Trim trim = new Trim();
		trim.applyNBT(compound);
		return trim;
	}

	
}
