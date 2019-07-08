package com.simibubi.mightyarchitect.control.compose;

import com.simibubi.mightyarchitect.control.design.DesignType;

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
	
	@Override
	public DesignType getRoofType() {
		DesignType roofType = super.getRoofType();
		switch (roofType) {
		case FLAT_ROOF:
			return DesignType.TOWER_FLAT_ROOF;
		case ROOF:
			return DesignType.TOWER_ROOF;
		default:
			return roofType;
		}
	}
	
}
