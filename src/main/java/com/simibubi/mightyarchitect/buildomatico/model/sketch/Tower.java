package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import net.minecraft.nbt.NBTTagCompound;

public class Tower extends Design {

	public int radius;
	
	@Override
	public Design fromNBT(NBTTagCompound compound) {
		Tower tower = new Tower();
		tower.applyNBT(compound);
		tower.radius = compound.getInteger("Radius");
		return tower;
	}

	@Override
	public String toString() {
		return super.toString() + "\nRADIUS " + radius;
	}
	
	@Override
	public boolean fitsHorizontally(int width) {
		return width == radius * 2 - 1;
	}


}
