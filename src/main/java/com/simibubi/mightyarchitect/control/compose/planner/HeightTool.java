package com.simibubi.mightyarchitect.control.compose.planner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class HeightTool extends AbstractRoomFaceSelectionTool {

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedRoom != null) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player.isSprinting()) {
				// Resize
				selectedRoom.height += scroll;
				selectedStack.forEachAbove(selectedRoom, room -> {
					room.y += scroll;
				});
				player.sendStatusMessage(new StringTextComponent("Height: " + TextFormatting.AQUA + selectedRoom.height + TextFormatting.WHITE + "m"), true);
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					room.move(0, scroll, 0);
				});
				player.sendStatusMessage(new StringTextComponent("Position: " + TextFormatting.AQUA + selectedRoom.y),
						true);
			}
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

	
}
