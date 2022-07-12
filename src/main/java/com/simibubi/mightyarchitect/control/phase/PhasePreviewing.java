package com.simibubi.mightyarchitect.control.phase;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;
import com.simibubi.mightyarchitect.gui.ToolSelectionScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class PhasePreviewing extends PhaseBase implements IRenderGameOverlay {

	private Tools activeTool;
	private ToolSelectionScreen toolSelection;

	@Override
	public void whenEntered() {
		final Consumer<Tools> callback = tool -> {
			equipTool(tool);
		};

		activeTool = Tools.RerollAll;
		activeTool.getTool()
			.init();
		List<Tools> tools = Tools.getWallDecorationTools();
		toolSelection = new ToolSelectionScreen(tools, callback);

		MightyClient.renderer.display(getModel());
	}

	private void equipTool(Tools tool) {
		if (tool == activeTool)
			return;
		activeTool = tool;
		activeTool.getTool()
			.init();
	}

	@Override
	public void onClick(int button) {
		if (button == 1) {
			if (Minecraft.getInstance().screen == null) {
				String message = activeTool.getTool()
					.handleRightClick();
				sendStatusMessage(message);
			}
		}
	}

	@Override
	public void onKey(int key, boolean released) {
		if (key == MightyClient.TOOL_MENU.getKey().getValue()) {
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
			Optional<KeyMapping> mapping = Arrays.stream(Minecraft.getInstance().options.keyHotbarSlots).filter(keyMapping -> keyMapping.getKey().getValue() == key).findFirst();
			if (mapping.isEmpty())
				return;

			toolSelection.select(ArrayUtils.indexOf(Minecraft.getInstance().options.keyHotbarSlots, mapping.get()));

			return;
		}

		activeTool.getTool().handleKeyInput(key);
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
		MightyClient.renderer.setActive(false);
	}

	@Override
	public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
		PoseStack ms = event.getMatrixStack();
		toolSelection.renderPassive(ms, event.getPartialTicks());
		activeTool.getTool()
			.renderOverlay(ms);
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Here is a preview of your new build.",
			"From here you can pick your materials in the palette picker [C]",
			"Once you are happy with what you see, save or build your structure.");
	}

}
