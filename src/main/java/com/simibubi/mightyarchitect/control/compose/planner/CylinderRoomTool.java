package com.simibubi.mightyarchitect.control.compose.planner;

import com.simibubi.mightyarchitect.AllSpecialTextures;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.foundation.utility.RaycastHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class CylinderRoomTool extends RoomTool {

	@Override
	public void init() {
		super.init();
		firstPosition = null;
		toolModeCtrl = null;
		toolModeNoCtrl = null;
	}

	@Override
	public void updateSelection() {
		LocalPlayer player = Minecraft.getInstance().player;
		transparentStacks.clear();

		BlockHitResult trace = RaycastHelper.rayTraceRange(player.level, player, 75);
		if (trace != null && trace.getType() == Type.BLOCK) {

			BlockPos hit = trace.getBlockPos();
			makeStacksTransparent(player, hit);

			if (trace.getDirection()
				.getAxis()
				.isVertical())
				hit = hit.relative(trace.getDirection());

			if (model.getAnchor() == null)
				selectedPosition = hit;
			else
				selectedPosition = hit.subtract(model.getAnchor());

		} else {
			selectedPosition = null;
		}

		if (firstPosition == null)
			return;

		if (selectedPosition == null)
			return;

	}

	@Override
	protected String createRoom(GroundPlan groundPlan) {
		int distance = (int) Math.sqrt(firstPosition.distSqr(selectedPosition));
		DesignTheme theme = groundPlan.theme;
		distance = Math.max(distance, theme.getStatistics().MinTowerRadius);
		distance = Math.min(distance, theme.getStatistics().MaxTowerRadius);
		BlockPos size = new BlockPos(distance * 2, 0, distance * 2);

		Room room = new Room(firstPosition, size);

		room.width++;
		room.length++;
		room.x -= distance;
		room.z -= distance;

		ThemeStatistics stats = theme.getStatistics();
		boolean hasFoundation = theme.getLayers()
			.contains(DesignLayer.Foundation);
		room.designLayer = hasFoundation ? DesignLayer.Foundation : DesignLayer.Regular;

		int radius = (room.width - 1) / 2;

		if (room.width != room.length) {
			return "Selection is not a circle: " + room.width + " != " + room.length;
		}
		if (room.width % 2 == 0D) {
			return "Tower cannot have even diameter: " + room.width;
		}
		if (radius < stats.MinTowerRadius) {
			return "Tower is too Thin (<" + stats.MinTowerRadius + "): " + radius;
		}
		if (radius > stats.MaxTowerRadius) {
			return "Tower radius is too large (>" + stats.MaxTowerRadius + "): " + radius;
		}

		if (radius > stats.MaxConicalRoofRadius || !stats.hasConicalRoof) {
			room.roofType = stats.hasFlatTowerRoof ? DesignType.FLAT_ROOF : DesignType.NONE;
		} else {
			room.roofType = stats.hasConicalRoof ? DesignType.ROOF : DesignType.NONE;
		}

		lastAddedStack = new CylinderStack(room);
		if (!adjustHeightForIntersection(groundPlan, room))
			room.height = theme.getDefaultHeightForFloor(0);

		groundPlan.addStack(lastAddedStack);
		firstPosition = null;
		return "New Tower has been added";
	}

	@Override
	public void tickToolOutlines() {
		if (selectedPosition == null)
			return;

		BlockPos anchor = ArchitectManager.getModel()
			.getAnchor();
		BlockPos cursorPos = (anchor != null) ? selectedPosition.offset(anchor) : selectedPosition;
		BlockPos previouslySelectedPos = (firstPosition != null) ? firstPosition.offset(anchor) : cursorPos;

		if (firstPosition == null) {
			MightyClient.outliner.chaseAABB(outlineKey, new AABB(cursorPos))
				.withFaceTexture(AllSpecialTextures.CHECKERED)
				.disableLineNormals()
				.colored(0xaa000000);
			return;
		}

		int distance = (int) Math.sqrt(firstPosition.distSqr(selectedPosition));
		DesignTheme theme = ArchitectManager.getModel()
			.getGroundPlan().theme;
		distance = Math.max(distance, theme.getStatistics().MinTowerRadius);
		distance = Math.min(distance, theme.getStatistics().MaxTowerRadius);
		BlockPos size = new BlockPos(distance * 2, 0, distance * 2);
		Cuboid selection = new Cuboid(previouslySelectedPos, size.getX(), 1, size.getZ());

		selection.width += 1;
		selection.length += 1;
		selection.x -= distance;
		selection.z -= distance;

		MightyClient.outliner.chaseAABB(outlineKey, selection.toAABB())
			.withFaceTexture(AllSpecialTextures.CHECKERED)
			.disableLineNormals()
			.colored(0xaa000000);

		drawTextAroundBounds(selection);
	}

}
