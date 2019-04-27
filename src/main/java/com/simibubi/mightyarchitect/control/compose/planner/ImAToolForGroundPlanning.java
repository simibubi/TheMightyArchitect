package com.simibubi.mightyarchitect.control.compose.planner;

public interface ImAToolForGroundPlanning {

	public String handleRightClick();
	public boolean handleMouseWheel(int scroll);
	
	public void updateSelection();
	
	public void renderTool();
	public void renderGroundPlan();
	
	public void init();
}
