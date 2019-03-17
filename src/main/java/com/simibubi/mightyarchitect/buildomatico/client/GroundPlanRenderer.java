package com.simibubi.mightyarchitect.buildomatico.client;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class GroundPlanRenderer {

	public void renderGroundPlan(GroundPlan groundPlan, BlockPos anchor) {
		if (groundPlan != null && anchor != null) {
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			TesselatorTextures.Trim.bind();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			groundPlan.forEachStack(stack -> {
				stack.forEach(room -> {

					BlockPos pos = room.getOrigin().add(anchor);
					TessellatorHelper.walls(bufferBuilder, pos, new BlockPos(room.width, 1, room.length), 0.125, false,
							true);

					if (room == stack.highest())
						TessellatorHelper.walls(bufferBuilder, pos.add(0, room.height, 0),
								new BlockPos(room.width, 1, room.length), 0.125, false, true);

				});
			});

			Tessellator.getInstance().draw();
			TesselatorTextures.Room.bind();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			groundPlan.forEachRoom(room -> {
				BlockPos pos = room.getOrigin().add(anchor);
				TessellatorHelper.cube(bufferBuilder, pos, room.getSize(), 0, false, false);
			});

			Tessellator.getInstance().draw();
		}
	}

}
