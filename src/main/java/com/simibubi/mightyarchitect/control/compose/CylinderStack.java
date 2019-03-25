package com.simibubi.mightyarchitect.control.compose;

public class CylinderStack extends Stack {

	public CylinderStack(Room room) {
		super(room);
	}

	public int getMaxFacadeWidth() {
		return theme.getStatistics().MaxTowerRadius * 2 + 1;
	}
	
	public int getMinWidth() {
		return theme.getStatistics().MinTowerRadius * 2 + 1;
	}
	
	
}
