package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;

import net.minecraft.util.math.BlockPos;

public abstract class GroundPlanningToolBase implements ImAToolForGroundPlanning {

	protected GroundPlannerClient planner;
	protected BlockPos selectedPosition;
	
	public GroundPlanningToolBase(GroundPlannerClient planner) {
		this.planner = planner;
		this.selectedPosition = BlockPos.ORIGIN;
	}
	
	@Override
	public void updateSelection(BlockPos selectedPos) {
		this.selectedPosition = selectedPos;
	}
	
	@Override
	public String handleRightClick() {
		if (selectedPosition == null)
			return null;
		
		if (planner.getAnchor() == null) {
			planner.setAnchor(selectedPosition);
			selectedPosition = BlockPos.ORIGIN;
		}
		
		return null;
	}
	
}
