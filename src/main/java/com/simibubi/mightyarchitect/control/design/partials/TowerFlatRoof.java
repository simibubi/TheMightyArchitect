package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundNBT;

public class TowerFlatRoof extends TowerRoof {

	@Override
	public Design fromNBT(CompoundNBT compound) {
		TowerFlatRoof towerRoof = new TowerFlatRoof();
		towerRoof.applyNBT(compound);
		towerRoof.radius = compound.getInteger("Radius");
		towerRoof.defaultWidth = towerRoof.radius * 2 + 1;
		return towerRoof;
	}
	
}
