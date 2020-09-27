package com.simibubi.mightyarchitect.control.compose.planner;

public interface IComposerTool {
	
	static Object toolOutlineKey = new Object();

	public String handleRightClick();
	public boolean handleMouseWheel(int scroll);
	
	public void tickToolOutlines();
	public void tickGroundPlanOutlines();
	
	public void updateSelection();
	public void renderOverlay();
	public void init();
}
