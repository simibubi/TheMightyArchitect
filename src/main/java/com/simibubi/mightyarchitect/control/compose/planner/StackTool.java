package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;

import net.minecraft.util.text.TextFormatting;

public class StackTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		highlightRoom = false;
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedStack != null) {
			if (scroll > 0) {
				model.getTheme().getStatistics();
				if (selectedStack.floors() < ThemeStatistics.MAX_FLOORS)
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

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (selectedStack != null) {
			status("Floors: " + TextFormatting.AQUA + selectedStack.floors());
		}
	}

}
