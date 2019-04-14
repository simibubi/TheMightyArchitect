package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

public interface IArchitectPhase {

	public void whenEntered();
	public void update();
	public void render();
	public void whenExited();
	
	public List<String> getToolTip();
	
	public void onClick(int button);
	public void onKey(int key);
	public void onScroll(int amount);
	
}
