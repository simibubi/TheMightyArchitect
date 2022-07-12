package com.simibubi.mightyarchitect.control.compose.planner;

import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.ChatFormatting;

public class StackTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		toolModeNoCtrl = "Grow/Shrink stack";
		toolModeCtrl = "Clone/Remove room";
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedStack == null)
			return super.handleMouseWheel(scroll);

		boolean ctrl = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		GroundPlan groundPlan = ArchitectManager.getModel()
			.getGroundPlan();

		if (ctrl && selectedRoom == null)
			return super.handleMouseWheel(scroll);

		if (scroll > 0) {
			if (!ctrl) {
				RoomTool.increaseMatchingOthers(groundPlan, selectedStack);
				return true;
			}
			selectedStack.insertNewAt(selectedStack.getRooms()
				.indexOf(selectedRoom) + 1, true);
			return true;
		}

		if (!ctrl)
			selectedStack.decrease();
		else
			selectedStack.removeRoom(selectedRoom);

		if (selectedStack.floors() == 0) {
			groundPlan.remove(selectedStack);
			selectedRoom = null;
			selectedStack = null;
			selectedFace = null;
		}
		return true;
	}

	@Override
	protected boolean isRoomHighlighted(Room room) {
		return super.isRoomHighlighted(room) || !highlightRoom && selectedStack != null && selectedStack.getRooms()
			.contains(room);
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
		highlightRoom = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);

		if (selectedStack != null)
			status("Floors: " + ChatFormatting.AQUA + selectedStack.floors());
	}

	public static void insertMatchingOthers(GroundPlan groundPlan, Stack stack, int index) {
		Room added = stack.insertNewAt(index + 1, true);
		if (added == null)
			return;

		int prevHeight = added.height;
		final MutableObject<Room> biggestRoom = new MutableObject<>();
		groundPlan.forEachRoom(r -> {
			if (r == added)
				return;
			if (r.intersects(added) && r.y <= added.y && r.y + r.height > added.y && (biggestRoom.getValue() == null
				|| biggestRoom.getValue().width * biggestRoom.getValue().length < r.width * r.length))
				biggestRoom.setValue(r);
		});
		if (biggestRoom.getValue() != null)
			added.height = (biggestRoom.getValue().y + biggestRoom.getValue().height) - added.y;
		int diff = added.height - prevHeight;
		stack.forEachAbove(added, r -> r.move(0, diff, 0));
	}

}
