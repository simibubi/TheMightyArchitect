package com.simibubi.mightyarchitect.control.phase;

public interface IArchitectPhase {

	public void whenEntered();
	public void update();
	public void render();
	public void whenExited();
	
	public void onClick(int button);
	public void onKey(int key);
	
}
