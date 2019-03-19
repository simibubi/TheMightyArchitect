package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.control.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper.PredicateTraceResult;
import com.simibubi.mightyarchitect.gui.GuiComposer;
import com.simibubi.mightyarchitect.gui.GuiOpener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class SelectionTool extends GroundPlanningToolBase {

	private Stack selectedStack;

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void updateSelection() {
		final GroundPlan groundPlan = ArchitectManager.getModel().getGroundPlan();
		final BlockPos anchor = ArchitectManager.getModel().getAnchor();
		
		if (groundPlan.isEmpty()) {
			selectedStack = null;
			return;
		}

		EntityPlayerSP player = Minecraft.getMinecraft().player;

		PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, position -> {
			return groundPlan.getRoomAtPos(position.subtract(anchor)) != null;
		});

		if (result.missed()) {
			selectedStack = null;
			return;
		}

		selectedStack = groundPlan.getStackAtPos(result.getPos().subtract(anchor));
	}

	@Override
	public String handleRightClick() {
		if (selectedStack == null) {
			return "Point at the room to modify";
		}

		GuiOpener.open(new GuiComposer(selectedStack));
		return null;
	}

	@Override
	public void render() {
		if (selectedStack == null)
			return;

		TesselatorTextures.SelectedRoom.bind();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		selectedStack.forEach(room -> {
			BlockPos pos = room.getOrigin().add(ArchitectManager.getModel().getAnchor());
			TessellatorHelper.cube(bufferBuilder, pos, room.getSize(), 1 / 16d, true, true);
		});

		Tessellator.getInstance().draw();
	}

}
