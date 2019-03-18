package com.simibubi.mightyarchitect.control.compose.planner;

public interface ImAToolForGroundPlanning {

	public String handleRightClick();
	
	public void handleKey(int key);
	public void updateSelection();
	public void render();
	public void init();
}
