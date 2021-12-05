package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundTag;

import com.simibubi.mightyarchitect.control.design.partials.Wall.ExpandBehaviour;

public class Facade extends Wall {

	@Override
	public Design fromNBT(CompoundTag compound) {
		Facade facade = new Facade();
		facade.expandBehaviour = ExpandBehaviour.None;
		facade.applyNBT(compound);
		return facade;
	}
	
}
