package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.gui.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
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
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				// Resize
				if (selectedRoom.height + scroll > model.getTheme().getMaxFloorHeight()) {
					selectedRoom.height = model.getTheme().getMaxFloorHeight();
					player.sendStatusMessage(new StringTextComponent("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m (max)"), true);
					return true;
				}
				if (selectedRoom.height + scroll < 1) {
					selectedRoom.height = 1;
					player.sendStatusMessage(new StringTextComponent("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m (min)"), true);
					return true;
				}
				
				selectedRoom.height += scroll;
				selectedStack.forEachAbove(selectedRoom, room -> {
					room.y += scroll;
				});
				player.sendStatusMessage(new StringTextComponent("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m"), true);
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedStack.lowest(), room -> {
					room.move(0, scroll, 0);
				});
				player.sendStatusMessage(new StringTextComponent("Position: " + TextFormatting.AQUA + selectedStack.lowest().y),
						true);
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
