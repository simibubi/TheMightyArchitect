package com.simibubi.mightyarchitect.control.phase;

import org.lwjgl.input.Keyboard;

import com.simibubi.mightyarchitect.control.compose.planner.Tools;
import com.simibubi.mightyarchitect.control.helpful.ShaderManager;
import com.simibubi.mightyarchitect.control.helpful.Shaders;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;

public class PhaseComposing extends PhaseBase implements IRenderGameOverlay {

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
			activeTool.getTool().init();
			return;
		}

		if (key == Keyboard.KEY_LEFT) {
			activeTool = activeTool.previous();
			activeTool.getTool().init();
			return;
		}

		activeTool.getTool().handleKey(key);
	}

	@Override
	public void render() {
		TessellatorHelper.prepareForDrawing();
		activeTool.getTool().renderGroundPlan();
		activeTool.getTool().renderTool();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	@Override
	public void whenExited() {
		ShaderManager.stopUsingShaders();
	}

	@Override
	public void renderGameOverlay(Post event) {
		ScaledResolution scaledresolution = new ScaledResolution(minecraft);
		minecraft.fontRenderer.drawString(activeTool.getDisplayName(), scaledresolution.getScaledWidth() / 2 + 15,
				scaledresolution.getScaledHeight() / 2 + 5, 0xDDDDDD, true);
	}

}
