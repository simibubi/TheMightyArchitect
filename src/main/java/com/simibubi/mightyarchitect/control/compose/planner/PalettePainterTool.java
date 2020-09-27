package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.util.text.TextFormatting;

public class PalettePainterTool extends WallDecorationToolBase {

	@Override
	public void init() {
		super.init();

		highlightStack = true;
		highlightRoom = false;
		highlightRoof = false;
		
		toolModeNoCtrl = "Stack";
		toolModeCtrl = "Room";
	}

	@Override
	public boolean handleMouseWheel(int amount) {

		if (model.getPrimary().getName().equals(model.getSecondary().getName())) {
			status(TextFormatting.RED + "Choose a secondary Palette first [ G -> C ]");
			return true;
		}

		if (selectedRoom == null) {
			status("Point at the Room to modify.");
			return false;
		}

		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			// Paint Room
			selectedRoom.secondaryPalette ^= true;
			
		} else {
			// Paint Stack
			boolean secondary = !selectedStack.lowest().secondaryPalette;
			selectedStack.forEach(room -> room.secondaryPalette = secondary);
		}
		
		ArchitectManager.reAssemble();
		status(selectedRoom.secondaryPalette ? "Secondary Palette" : "Primary Palette");
		return true;
	}
	
	@Override
	public void updateSelection() {
		super.updateSelection();
		
		highlightRoom = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		highlightStack = !highlightRoom;
		highlightRoof = highlightRoom;
	}

}
