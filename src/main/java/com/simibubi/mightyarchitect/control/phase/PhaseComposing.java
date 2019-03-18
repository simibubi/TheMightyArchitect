package com.simibubi.mightyarchitect.control.phase;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.helpful.Shaders;
import com.simibubi.mightyarchitect.control.helpful.ShaderManager;
import com.simibubi.mightyarchitect.control.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.planner.Tools;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class PhaseComposing extends PhaseBase {

	private Tools activeTool;

	@Override
	public void whenEntered() {
		activeTool = Tools.Room;
		activeTool.getTool().init();
		
		ShaderManager.setActiveShader(Shaders.Blueprint);
	}

	@Override
	public void update() {
		activeTool.getTool().updateSelection();
	}

	@Override
	public void onClick(int button) {
		if (button == 1) {
			String message = activeTool.getTool().handleRightClick();
			sendStatusMessage(message);
		}

	}

	@Override
	public void onKey(int key) {
		if (key == Keyboard.KEY_RIGHT) {
			activeTool = activeTool.next();
			return;
		}
		
		if (key == Keyboard.KEY_LEFT) {
			activeTool = activeTool.previous();
			return;
		}
		
		activeTool.getTool().handleKey(key);
	}

	@Override
	public void render() {
		TessellatorHelper.prepareForDrawing();
		Schematic model = getModel();
		renderGroundPlan(model.getGroundPlan(), model.getAnchor());
		activeTool.getTool().render();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	@Override
	public void whenExited() {
		ShaderManager.stopUsingShaders();
	}

	protected void renderGroundPlan(GroundPlan groundPlan, BlockPos anchor) {
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
