package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public abstract class AbstractRoomFaceSelectionTool extends GroundPlanningToolBase {

	protected boolean highlightRoom;

	@Override
	public void init() {
		super.init();
		highlightRoom = true;
	}

	@Override
	protected void makeStacksTransparent(LocalPlayer player, BlockPos hit) {}

	@Override
	public void tickToolOutlines() {}

	@Override
	protected boolean isStackHighlighted(Stack stack) {
		return stack == selectedStack;
	}

	@Override
	protected boolean isRoomHighlighted(Room room) {
		return room == selectedRoom && highlightRoom;
	}

}
