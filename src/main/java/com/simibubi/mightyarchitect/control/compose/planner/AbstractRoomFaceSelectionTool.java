package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper.PredicateTraceResult;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractRoomFaceSelectionTool extends GroundPlanningToolBase {

	public static Stack selectedStack;
	public static Room selectedRoom;
	public static Direction selectedFace;

	protected boolean highlightRoom;

	@Override
	public void init() {
		super.init();
		highlightRoom = true;
		selectedStack = null;
		selectedFace = null;
		selectedRoom = null;
	}

	@Override
	public void updateSelection() {
		updateOverlay();
		
		final GroundPlan groundPlan = ArchitectManager.getModel().getGroundPlan();
		final BlockPos anchor = ArchitectManager.getModel().getAnchor();

		if (groundPlan.isEmpty()) {
			selectedStack = null;
			selectedFace = null;
			selectedRoom = null;
			return;
		}

		ClientPlayerEntity player = Minecraft.getInstance().player;

		PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, position -> {
			return groundPlan.getRoomAtPos(position.subtract(anchor)) != null;
		});

		if (result.missed()) {
			selectedStack = null;
			selectedFace = null;
			selectedRoom = null;
			return;
		}

		BlockPos pos = result.getPos().subtract(anchor);
		selectedRoom = groundPlan.getRoomAtPos(pos);
		selectedStack = groundPlan.getStackAtPos(pos);
		selectedFace = result.getFacing();
	}

	@Override
	public void renderTool() {
		if (selectedStack == null)
			return;

		TessellatorTextures.SelectedRoom.bind();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		selectedStack.forEach(room -> {
			if (room == selectedRoom && highlightRoom)
				return;
			BlockPos pos = room.getOrigin().add(ArchitectManager.getModel().getAnchor());
			TessellatorHelper.cube(bufferBuilder, pos, room.getSize(), 1 / 16d, true, true);
		});

		Tessellator.getInstance().draw();

		if (selectedRoom != null && highlightRoom) {
			TessellatorTextures.SuperSelectedRoom.bind();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			BlockPos pos = selectedRoom.getOrigin().add(ArchitectManager.getModel().getAnchor());
			TessellatorHelper.cube(bufferBuilder, pos, selectedRoom.getSize(), 1 / 16d, true, true);

			Tessellator.getInstance().draw();
		}

	}

	

}
