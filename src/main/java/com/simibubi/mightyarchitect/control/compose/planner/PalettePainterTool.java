package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.ArchitectManager;

import net.minecraft.util.text.TextFormatting;

public class PalettePainterTool extends WallDecorationToolBase {

	@Override
	public void init() {
		super.init();
		highlightRoom = true;
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

		selectedRoom.secondaryPalette ^= true;
		ArchitectManager.reAssemble();
		status(selectedRoom.secondaryPalette ? "Secondary Palette" : "Primary Palette");
		return true;
	}

}
