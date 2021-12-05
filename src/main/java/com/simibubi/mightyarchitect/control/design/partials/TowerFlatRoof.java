package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.CompoundTag;

public class TowerFlatRoof extends TowerRoof {

	@Override
	public Design fromNBT(CompoundTag compound) {
		TowerFlatRoof towerRoof = new TowerFlatRoof();
		towerRoof.applyNBT(compound);
		towerRoof.radius = compound.getInt("Radius");
		towerRoof.defaultWidth = towerRoof.radius * 2 + 1;
		return towerRoof;
	}
	
}
