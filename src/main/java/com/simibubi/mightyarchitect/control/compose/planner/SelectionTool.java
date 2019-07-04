package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.gui.GuiComposer;
import com.simibubi.mightyarchitect.gui.GuiOpener;

public class SelectionTool extends AbstractRoomFaceSelectionTool {

	@Override
	public String handleRightClick() {
		if (selectedStack == null) {
			return "Point at the room to modify";
		}

		GuiOpener.open(new GuiComposer(selectedStack));
		return null;
	}

}
