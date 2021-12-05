package com.simibubi.mightyarchitect.control.compose.planner;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IComposerTool {
	
	static Object toolOutlineKey = new Object();

	public String handleRightClick();
	public boolean handleMouseWheel(int scroll);
	
	public void tickToolOutlines();
	public void tickGroundPlanOutlines();
	
	public void updateSelection();
	public void renderOverlay(PoseStack ms);
	public void init();
}
