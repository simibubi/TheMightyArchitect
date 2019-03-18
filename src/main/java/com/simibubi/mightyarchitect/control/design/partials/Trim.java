package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.NBTTagCompound;

public class Trim extends Design {

	@Override
	public Design fromNBT(NBTTagCompound compound) {
		Trim trim = new Trim();
		trim.applyNBT(compound);
		return trim;
	}

	
}
