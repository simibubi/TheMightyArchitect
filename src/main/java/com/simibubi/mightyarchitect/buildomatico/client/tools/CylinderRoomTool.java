package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;

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

}
