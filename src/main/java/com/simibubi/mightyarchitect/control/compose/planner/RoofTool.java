package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.DesignType;

import net.minecraft.util.text.TextFormatting;

public class RoofTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		highlightRoom = false;
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedStack == null)
			return super.handleMouseWheel(scroll);

		Room room = selectedStack.highest();

		if (room.roofType == DesignType.ROOF) {
			if (!(selectedStack instanceof CylinderStack) && !room.quadFacadeRoof && room.width == room.length) {
				room.quadFacadeRoof = true;
			} else {
				room.roofType = DesignType.FLAT_ROOF;
				room.quadFacadeRoof = false;
			}

		} else if (room.roofType == DesignType.FLAT_ROOF) {
			room.roofType = DesignType.NONE;

		} else if (room.roofType == DesignType.NONE) {
			room.roofType = DesignType.ROOF;

		}

		room.roofType = model.getTheme()
			.getStatistics()
			.fallbackRoof(room, selectedStack instanceof CylinderStack);
		return true;

	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (selectedStack == null)
			return;
		if (selectedStack.highest().quadFacadeRoof)
			status("Roof Type: " + TextFormatting.AQUA + "4-Facade Gable Roof");
		else
			status("Roof Type: " + TextFormatting.AQUA + selectedStack.highest().roofType.getDisplayName());
	}


}
