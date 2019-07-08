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
		if (selectedStack != null) {
			Room room = selectedStack.highest();
			switch (room.roofType) {
			case FLAT_ROOF:
				room.roofType = DesignType.NONE;
				break;
			default:
			case NONE:
				room.roofType = DesignType.ROOF;
				break;
			case ROOF:
				room.roofType = DesignType.FLAT_ROOF;
				break;
			}
			room.roofType = model.getTheme().getStatistics().fallbackRoof(room, selectedStack instanceof CylinderStack);
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (selectedStack != null) {
			status("Roof Type: " + TextFormatting.AQUA + selectedStack.highest().roofType.getDisplayName());
		}
	}

	@Override
	public void renderGroundPlan() {
		super.renderGroundPlan();
	}

}
