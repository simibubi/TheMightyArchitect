package com.simibubi.mightyarchitect.buildomatico.client.tools;

import net.minecraft.util.math.BlockPos;

public interface ImAToolForGroundPlanning {

	public String handleRightClick();
	
	public void updateSelection(BlockPos selectedPos);
	public void render();
	
}
