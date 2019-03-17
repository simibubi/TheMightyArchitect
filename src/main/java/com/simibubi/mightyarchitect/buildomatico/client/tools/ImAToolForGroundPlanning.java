package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;

public interface ImAToolForGroundPlanning {

	public String handleRightClick();
	
	public void handleKey(int key);
	public void updateSelection();
	public void render();
	public void init(GroundPlannerClient planner);
}
