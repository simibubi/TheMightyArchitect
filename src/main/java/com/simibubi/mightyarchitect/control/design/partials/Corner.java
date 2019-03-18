package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.NBTTagCompound;

public class Corner extends Design {
	
	@Override
	public Design fromNBT(NBTTagCompound compound) {
		Corner corner = new Corner();
		corner.applyNBT(compound);
		return corner;
	}

}
