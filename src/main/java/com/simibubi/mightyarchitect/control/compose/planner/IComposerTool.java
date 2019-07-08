package com.simibubi.mightyarchitect.control.compose.planner;

public interface IComposerTool {

	public String handleRightClick();
	public boolean handleMouseWheel(int scroll);
	
	public void updateSelection();
	
	public void renderTool();
	public void renderGroundPlan();
	public void renderOverlay();
	
	public void init();
}
