package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;

public class MoveReshapeTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		toolModeNoCtrl = "Move (XZ)";
		toolModeCtrl = "Resize (XZ)";
	}

	@Override
	protected boolean isRoomHighlighted(Room room) {
		boolean isSelected = super.isRoomHighlighted(room);
		if (isSelected)
			return true;
		if (selectedStack == null || selectedRoom == null)
			return false;
		List<Room> rooms = selectedStack.getRooms();
		if (!rooms.contains(selectedRoom) || !rooms.contains(room))
			return false;
		return rooms.indexOf(selectedRoom) <= rooms.indexOf(room);
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedRoom != null) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				// Resize
				ThemeStatistics statistics = model.getTheme()
					.getStatistics();
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.relative(selectedFace, scroll);

					if (selectedStack instanceof CylinderStack) {
						if (scroll < 0 && room.width < statistics.MaxTowerRadius * 2 + 1) {
							room.x--;
							room.z--;
							room.width += 2;
							room.length += 2;
						}
						if (scroll > 0 && room.width > statistics.MinTowerRadius * 2 + 1) {
							room.x++;
							room.z++;
							room.width -= 2;
							room.length -= 2;
						}

						return;
					}

					int faceDirection = selectedFace.getAxisDirection()
						.getStep();
					int newWidth = room.width - 2 * diff.getX() * faceDirection;
					int newLength = room.length - 2 * diff.getZ() * faceDirection;

					if (Math.min(newWidth, newLength) < statistics.MinRoomLength)
						return;
					if (Math.max(newWidth, newLength) > statistics.MaxRoomLength)
						return;

					room.x += diff.getX() * faceDirection;
					room.width = newWidth;
					room.z += diff.getZ() * faceDirection;
					room.length = newLength;
				});
				selectedStack.highest().roofType =
					statistics.fallbackRoof(selectedStack.highest(), selectedStack instanceof CylinderStack);
				status("Size: " + ChatFormatting.AQUA + selectedRoom.width + ChatFormatting.WHITE + "x"
					+ ChatFormatting.AQUA + selectedRoom.length);
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.relative(selectedFace, scroll);
					room.move(-diff.getX(), 0, -diff.getZ());
				});
				status("Position: " + ChatFormatting.AQUA + selectedRoom.x + ChatFormatting.WHITE + ", "
					+ ChatFormatting.AQUA + selectedRoom.z);
			}
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

	@Override
	public boolean numberInputSimulatesScrolls() {
		return true;
	}
}
