package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.Room;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class WallDecorationToolBase extends ComposerToolBase {

	protected boolean highlightStack;
	protected boolean highlightRoom;
	protected boolean highlightRoof;

	@Override
	public void init() {
		super.init();
		highlightStack = false;
		highlightRoom = false;
		highlightRoof = false;
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		return false;
	}

	@Override
	public String handleRightClick() {
		return null;
	}

	@Override
	public void tickGroundPlanOutlines() {}

	@Override
	public void tickToolOutlines() {
		
		if (highlightRoom && selectedRoom != null) {
			BlockPos min = selectedRoom.getOrigin()
				.offset(model.getAnchor());
			BlockPos max = selectedRoom.getOrigin()
				.offset(selectedRoom.getSize())
				.offset(model.getAnchor());

			if (highlightRoof && selectedRoom == selectedStack.highest() && selectedFace == Direction.UP) {
				min = min.offset(0, selectedRoom.height, 0);
				max = max.offset(0, selectedRoom.height, 0);
			}

			MightyClient.outliner.chaseAABB(toolOutlineKey, new AxisAlignedBB(min.getX() - 1 / 2d,
				min.getY() + 1 / 4d, min.getZ() - 1 / 2d, max.getX() + 1 / 2d, max.getY(), max.getZ() + 1 / 2d)).lineWidth(1/8f);
			return;
		}
		
		if (!highlightStack || selectedStack == null)
			return;

		Cuboid stack = new Cuboid(selectedStack.lowest()
			.getOrigin(), BlockPos.ZERO);
		stack.height = selectedStack.highest().y + selectedStack.highest().height;

		for (Room room : selectedStack.getRooms()) {
			if (stack.x > room.x) {
				stack.width += stack.x - room.x;
				stack.x = room.x;
			}
			if (stack.z > room.z) {
				stack.length += stack.z - room.z;
				stack.z = room.z;
			}
			if (stack.x + stack.width < room.x + room.width) {
				stack.width += (room.x + room.width) - (stack.x + stack.width);
			}
			if (stack.z + stack.length < room.z + room.length) {
				stack.length += (room.z + room.length) - (stack.z + stack.length);
			}
		}

		BlockPos min = stack.getOrigin()
			.offset(model.getAnchor());
		BlockPos max = stack.getOrigin()
			.offset(stack.getSize())
			.offset(model.getAnchor());
		MightyClient.outliner.chaseAABB(toolOutlineKey, new AxisAlignedBB(min.getX() - 1 / 2d, min.getY() + 1 / 4d,
			min.getZ() - 1 / 2d, max.getX() + 1 / 2d, max.getY(), max.getZ() + 1 / 2d)).lineWidth(1/8f);
	}

}
