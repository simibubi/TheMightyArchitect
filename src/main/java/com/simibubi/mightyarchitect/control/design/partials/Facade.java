package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundNBT;

public class Facade extends Wall {

	@Override
	public Design fromNBT(CompoundNBT compound) {
		Facade facade = new Facade();
		facade.expandBehaviour = ExpandBehaviour.None;
		facade.applyNBT(compound);
		return facade;
	}
	
}
