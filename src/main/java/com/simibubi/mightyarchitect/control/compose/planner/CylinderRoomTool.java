package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.control.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class CylinderRoomTool extends RoomTool {

	@Override
	public void init() {
		super.init();
		firstPosition = null;
	}

	@Override
	public void updateSelection() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		transparentStacks.clear();

		RayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.typeOfHit == Type.BLOCK) {

			BlockPos hit = trace.getBlockPos();
			makeStacksTransparent(player, hit);
			
			if (trace.sideHit.getAxis() == Axis.Y)
				hit = hit.offset(trace.sideHit);

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
		int distance = (int) Math.sqrt(firstPosition.distanceSq(selectedPosition));
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
		boolean hasFoundation = theme.getLayers().contains(DesignLayer.Foundation);
		
		room.height = hasFoundation? 2 : Math.min(4, theme.getMaxFloorHeight());
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
			room.roofType = stats.hasFlatTowerRoof ?  DesignType.FLAT_ROOF : DesignType.NONE;
		} else {
			room.roofType = stats.hasConicalRoof ? DesignType.ROOF : DesignType.NONE;			
		}
				
		lastAddedStack = new CylinderStack(room);
		groundPlan.addStack(lastAddedStack);
		firstPosition = null;
		return "§aNew Tower has been added";
	}
	
	@Override
	public void renderTool() {
		
		if (selectedPosition == null) {
			return;
		}
		
		BlockPos anchor = ArchitectManager.getModel().getAnchor();
		BlockPos selectedPos = (anchor != null)? selectedPosition.add(anchor) : selectedPosition;
		BlockPos firstPos = (firstPosition != null)? firstPosition.add(anchor) : null;

		TesselatorTextures.Selection.bind();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		TessellatorHelper.walls(bufferBuilder, selectedPos, new BlockPos(1, 1, 1), 0.125, false, true);

		if (firstPos != null) {
			int distance = (int) Math.sqrt(firstPosition.distanceSq(selectedPosition));
			DesignTheme theme = ArchitectManager.getModel().getGroundPlan().theme;
			distance = Math.max(distance, theme.getStatistics().MinTowerRadius);
			distance = Math.min(distance, theme.getStatistics().MaxTowerRadius);
			BlockPos size = new BlockPos(distance * 2, 0, distance * 2);
			Cuboid selection = new Cuboid(firstPos, size.getX(), 1, size.getZ());
			
			selection.width += 1;
			selection.length += 1;
			selection.x -= distance;
			selection.z -= distance;
			
			TessellatorHelper.walls(bufferBuilder, selection.getOrigin(), selection.getSize(), -0.125, false, true);
			Tessellator.getInstance().draw();

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z - 1, true, false);

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z + selection.length + 1, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x + selection.width + 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x - 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

		} else {
			Tessellator.getInstance().draw();
		}
	}

}
