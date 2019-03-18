package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.CylinderStack;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Room;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;

import net.minecraft.util.math.BlockPos;

public class CylinderRoomTool extends RoomTool {

	@Override
	public void init(GroundPlannerClient planner) {
		super.init(planner);
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (firstPosition == null)
			return;

		if (selectedPosition == null)
			return;

		// selections can only be square
		BlockPos size = selectedPosition.subtract(firstPosition);
		Cuboid selection = new Cuboid(firstPosition, size);

		int width = selection.width;
		int length = selection.length;

		if (width == length)
			return;

		if (width > length)
			selectedPosition = selectedPosition.west((size.getX() > 0 ? 1 : -1) * (width - length));

		if (length > width)
			selectedPosition = selectedPosition.north((size.getZ() > 0 ? 1 : -1) * (length - width));

	}

	@Override
	protected String createRoom(GroundPlan groundPlan) {
		Room room = new Room(firstPosition, selectedPosition.subtract(firstPosition));
		room.width++;
		room.length++;
		room.height = 2;
		room.designLayer = DesignLayer.Foundation;
		int radius = (room.width + 1) / 2;

		if (room.width != room.length) {
			return "§cSelection is not a circle: " + room.width + " != " + room.length;
		}
		if (room.width % 2 == 0D) {
			return "§cTower cannot have even diameter: " + room.width;
		}
		if (radius < 2) {
			return "§cTower is too Thin (<2): " + radius;
		}
		if (radius > 6) {
			return "§cTower radius is too large (>6): " + radius;
		}

		room.roofType = DesignType.ROOF;
		lastAddedStack = new CylinderStack(room);
		groundPlan.addStack(lastAddedStack);
		firstPosition = null;
		return "§aNew Tower has been added";
	}

}
