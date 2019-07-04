package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.ArchitectManager;

public class StackTool extends AbstractRoomFaceSelectionTool {

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedStack != null) {
			if (scroll > 0) {
				selectedStack.increase();
				return true;
			} else {
				selectedStack.decrease();
				if (selectedStack.floors() == 0) {
					ArchitectManager.getModel().getGroundPlan().remove(selectedStack);
					selectedRoom = null;
					selectedStack = null;
					selectedFace = null;
				}
				return true;
			}
		}
		return super.handleMouseWheel(scroll);
	}
	
}
