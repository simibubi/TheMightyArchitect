package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public interface IComposerTool {
	
	Object toolOutlineKey = new Object();

	String handleRightClick();
	boolean handleMouseWheel(int scroll);

	default void handleKeyInput(int key) {
		if (!numberInputSimulatesScrolls())
			return;

		Optional<KeyMapping> mapping = Arrays.stream(Minecraft.getInstance().options.keyHotbarSlots).filter(keyMapping -> keyMapping.getKey().getValue() == key).findFirst();
		if (mapping.isEmpty())
			return;

		int number = ArrayUtils.indexOf(Minecraft.getInstance().options.keyHotbarSlots, mapping.get()) + 1;
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
			number = number * -1;
		}

		handleMouseWheel(number);

	}
	default boolean numberInputSimulatesScrolls() {
		return false;
	}

	void tickToolOutlines();
	void tickGroundPlanOutlines();
	
	void updateSelection();
	void renderOverlay(GuiGraphics graphics);
	void init();
}
