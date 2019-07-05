package com.simibubi.mightyarchitect.control.phase;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;
import com.simibubi.mightyarchitect.control.helpful.ShaderManager;
import com.simibubi.mightyarchitect.control.helpful.Shaders;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.gui.ToolSelectionScreen;

import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;

public class PhaseComposing extends PhaseBase implements IRenderGameOverlay {

	private Tools activeTool;
	private ToolSelectionScreen toolSelection;

	@Override
	public void whenEntered() {
		final Consumer<Tools> callback = tool -> {
			equipTool(tool);
		};

		activeTool = Tools.Room;
		activeTool.getTool().init();
		List<Tools> groundPlanningTools = Tools.getGroundPlanningTools();
		
		if (!getModel().getTheme().getStatistics().hasTowers)
			groundPlanningTools.remove(Tools.Cylinder);
		
		toolSelection = new ToolSelectionScreen(groundPlanningTools, callback);

		ShaderManager.setActiveShader(Shaders.Blueprint);
	}

	private void equipTool(Tools tool) {
		if (tool == activeTool)
			return;
		activeTool = tool;
		activeTool.getTool().init();
	}

	@Override
	public void update() {
		activeTool.getTool().updateSelection();
		toolSelection.update();
	}

	@Override
	public void onClick(int button) {
		if (button == 1) {
			String message = activeTool.getTool().handleRightClick();
			sendStatusMessage(message);
		}
	}

	@Override
	public void onKey(int key, boolean released) {
		if (key != TheMightyArchitect.TOOL_MENU.getKey().getKeyCode())
			return;

		if (released && toolSelection.focused) {
			toolSelection.focused = false;
			toolSelection.onClose();
		}

		if (!released && !toolSelection.focused)
			toolSelection.focused = true;

	}

	@Override
	public boolean onScroll(int amount) {
		if (toolSelection.focused) {
			toolSelection.cycle(amount);
			return true;
		}

		return activeTool.getTool().handleMouseWheel(amount);
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
		toolSelection.renderPassive(event.getPartialTicks());
		activeTool.getTool().renderOverlay();
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of(
				"Draw the layout of your build, adding rooms, towers and other. Modify their size, style and palette using the Selection Tool.",
				"Use your < > Arrow Keys to switch tools.");
	}

}
