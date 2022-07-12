package com.simibubi.mightyarchitect.control.phase;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;
import com.simibubi.mightyarchitect.foundation.utility.ShaderManager;
import com.simibubi.mightyarchitect.foundation.utility.Shaders;
import com.simibubi.mightyarchitect.gui.ToolSelectionScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class PhaseComposing extends PhaseBase implements IRenderGameOverlay {

	private Tools activeTool;
	private ToolSelectionScreen toolSelection;

	@Override
	public void whenEntered() {
		activeTool = Tools.Room;
		activeTool.getTool()
			.init();
		List<Tools> groundPlanningTools = Tools.getGroundPlanningTools();

		if (!getModel().getTheme()
			.getStatistics().hasTowers)
			groundPlanningTools.remove(Tools.Cylinder);

		toolSelection = new ToolSelectionScreen(groundPlanningTools, this::equipTool);
		ShaderManager.setActiveShader(Shaders.Blueprint);
	}

	private void equipTool(Tools tool) {
		if (tool == activeTool)
			return;
		activeTool = tool;
		activeTool.getTool()
			.init();
	}

	@Override
	public void update() {
		activeTool.getTool()
			.updateSelection();
		toolSelection.update();
		activeTool.getTool()
			.tickGroundPlanOutlines();
		activeTool.getTool()
			.tickToolOutlines();
	}

	@Override
	public void onClick(int button) {
		if (button != 1)
			return;
		String message = activeTool.getTool()
			.handleRightClick();
		sendStatusMessage(message);
	}

	@Override
	public void onKey(int key, boolean released) {
		if (key == MightyClient.TOOL_MENU.getKey()
			.getValue()) {
			if (released && toolSelection.focused) {
				toolSelection.focused = false;
				toolSelection.onClose();
			}

			if (!released && !toolSelection.focused)
				toolSelection.focused = true;

			return;
		}

		if (released)
			return;

		if (toolSelection.focused) {
			Optional<KeyMapping> mapping = Arrays.stream(Minecraft.getInstance().options.keyHotbarSlots)
				.filter(keyMapping -> keyMapping.getKey()
					.getValue() == key)
				.findFirst();
			if (mapping.isEmpty())
				return;

			toolSelection.select(ArrayUtils.indexOf(Minecraft.getInstance().options.keyHotbarSlots, mapping.get()));

			return;
		}

		activeTool.getTool()
			.handleKeyInput(key);
	}

	@Override
	public boolean onScroll(int amount) {
		if (toolSelection.focused) {
			toolSelection.cycle(amount);
			return true;
		}

		return activeTool.getTool()
			.handleMouseWheel(amount);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer) {}

	@Override
	public void whenExited() {
		ShaderManager.stopUsingShaders();
	}

	@Override
	public void renderGameOverlay(Pre event) {
		if (Minecraft.getInstance().screen != null)
			return;

		PoseStack ms = event.getMatrixStack();
		toolSelection.renderPassive(ms, Minecraft.getInstance()
			.getDeltaFrameTime());
		activeTool.getTool()
			.renderOverlay(ms);
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of(
			"Draw the layout of your build, adding rooms, towers and other. Modify their position, size and roof using the Tools.");
	}

}
