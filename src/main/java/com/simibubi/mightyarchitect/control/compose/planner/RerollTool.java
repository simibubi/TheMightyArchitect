package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.ArchitectManager;

public class RerollTool extends WallDecorationToolBase {

	@Override
	public boolean handleMouseWheel(int amount) {
		
		model.getTheme().getDesignPicker().rerollAll();
		ArchitectManager.reAssemble();
		
		return true;
	}
	
}
