package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

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
			status(ChatFormatting.RED + "Choose a secondary Palette first [ G -> C ]");
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
	public void handleKeyInput(int key) {
		Optional<KeyMapping> mapping = Arrays.stream(Minecraft.getInstance().options.keyHotbarSlots).filter(keyMapping -> keyMapping.getKey().getValue() == key).findFirst();
		if (mapping.isEmpty())
			return;

		int index = ArrayUtils.indexOf(Minecraft.getInstance().options.keyHotbarSlots, mapping.get());
		if (index > 1)
			return;

		if (model.getPrimary().getName().equals(model.getSecondary().getName())) {
			status(ChatFormatting.RED + "Choose a secondary Palette first [ G -> C ]");
			return;
		}

		if (selectedRoom == null) {
			status("Point at the Room to modify.");
			return;
		}

		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			// Paint Room
			selectedRoom.secondaryPalette = index == 1;

		} else {
			// Paint Stack
			selectedStack.forEach(room -> room.secondaryPalette = index == 1);
		}

		ArchitectManager.reAssemble();
		status(selectedRoom.secondaryPalette ? "Secondary Palette" : "Primary Palette");
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
		
		highlightRoom = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		highlightStack = !highlightRoom;
		highlightRoof = highlightRoom;
	}

}
