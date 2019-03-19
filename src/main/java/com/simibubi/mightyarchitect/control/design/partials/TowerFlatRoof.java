package com.simibubi.mightyarchitect.control.design.partials;

import net.minecraft.nbt.NBTTagCompound;

public class TowerFlatRoof extends TowerRoof {

	@Override
	public Design fromNBT(NBTTagCompound compound) {
		TowerFlatRoof towerRoof = new TowerFlatRoof();
		towerRoof.applyNBT(compound);
		towerRoof.radius = compound.getInteger("Radius");
		towerRoof.defaultWidth = towerRoof.radius * 2 + 1;
		return towerRoof;
	}
	
}
