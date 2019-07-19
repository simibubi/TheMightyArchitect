package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.helpful.Keyboard;

import net.minecraft.util.Direction;

public class RerollTargetTool extends WallDecorationToolBase {

	@Override
	public void init() {
		super.init();
		highlightStack = true;
		highlightRoom = false;
		highlightRoof = false;
		
		toolModeNoCtrl = "Stack";
		toolModeCtrl = "Room / Roof";
	}
	
	@Override
	public boolean handleMouseWheel(int amount) {
		
		boolean keyDown = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		if (selectedRoom != null && keyDown) {
			model.seed += amount;
			
			if (selectedFace == Direction.UP) {
				model.getTheme().getDesignPicker().rerollRoof(selectedStack);
				ArchitectManager.reAssemble();
				return true;
			}
			
			model.getTheme().getDesignPicker().rerollRoom(selectedRoom);
			ArchitectManager.reAssemble();
			return true;
		}
		
		if (selectedStack != null && !keyDown) {
			model.seed += amount;
			model.getTheme().getDesignPicker().rerollStack(selectedStack);
			ArchitectManager.reAssemble();
			return true;
		}
		
		return super.handleMouseWheel(amount);
	}
	
	@Override
	public void updateSelection() {
		super.updateSelection();
		
		highlightRoom = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		highlightStack = !highlightRoom;
		highlightRoof = highlightRoom;
	}
	
}
