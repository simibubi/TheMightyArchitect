package com.simibubi.mightyarchitect.control.compose.planner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class MoveReshapeTool extends AbstractRoomFaceSelectionTool {

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedRoom != null) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player.isSprinting()) {
				// Resize
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.offset(selectedFace, scroll);
					room.x += diff.getX();
					room.width -= diff.getX();
					room.height -= diff.getY();
					room.z -= diff.getZ();
					room.length -= diff.getZ();
				});
				player.sendStatusMessage(new StringTextComponent("Size: " + TextFormatting.AQUA + selectedRoom.width
						+ TextFormatting.WHITE + "x" + TextFormatting.AQUA + selectedRoom.length), true);
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.offset(selectedFace, scroll);
					room.move(-diff.getX(), diff.getY(), -diff.getZ());
				});
				player.sendStatusMessage(new StringTextComponent("Position: " + TextFormatting.AQUA + selectedRoom.x + TextFormatting.WHITE + ", " + TextFormatting.AQUA + selectedRoom.z),
						true);
			}
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

}
