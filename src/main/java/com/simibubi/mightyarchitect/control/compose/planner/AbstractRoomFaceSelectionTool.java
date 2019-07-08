package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractRoomFaceSelectionTool extends GroundPlanningToolBase {

	protected boolean highlightRoom;

	@Override
	public void init() {
		super.init();
		highlightRoom = true;
	}

	@Override
	protected void makeStacksTransparent(ClientPlayerEntity player, BlockPos hit) {
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
