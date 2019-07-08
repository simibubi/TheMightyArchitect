package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.gui.Keyboard;

import net.minecraft.util.text.TextFormatting;

public class HeightTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		toolModeNoCtrl = "Move Stack (Y)";
		toolModeCtrl = "Resize (Y)";
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedRoom != null) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				// Resize
				if (selectedRoom.height + scroll > model.getTheme().getMaxFloorHeight()) {
					selectedRoom.height = model.getTheme().getMaxFloorHeight();
					status("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m (max)");
					return true;
				}
				if (selectedRoom.height + scroll < 1) {
					selectedRoom.height = 1;
					status("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m (min)");
					return true;
				}

				selectedRoom.height += scroll;
				selectedStack.forEachAbove(selectedRoom, room -> {
					room.y += scroll;
				});
				status("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m");
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedStack.lowest(), room -> {
					room.move(0, scroll, 0);
				});
				status("Position: " + TextFormatting.AQUA + selectedStack.lowest().y);
			}
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
		highlightRoom = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
	}

}
