package com.simibubi.mightyarchitect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public enum Keybinds {

	ACTIVATE("activate", Type.KEYSYM, GLFW.GLFW_KEY_G),
	FOCUL_TOOL_MENU("focus_tool_menu", Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT),

	;

	public static interface KeybindListener {
		void keybindTriggered(boolean pressed);
	}

	private KeyMapping keybind;
	private String description;
	private int key;
	private boolean modifiable;
	private List<KeybindListener> callbacks;
	private Type initialType;

	private Keybinds(int fixedKey, Type initialType) {
		this("", initialType, fixedKey);
	}

	private Keybinds(String description, Type initialType, int defaultKey) {
		this.initialType = initialType;
		this.description = TheMightyArchitect.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.callbacks = new ArrayList<>();
	}

	public void notifyMe(KeybindListener callback) {
		callbacks.add(callback);
	}

	public static void register(RegisterKeyMappingsEvent event) {
		for (Keybinds key : values()) {
			key.keybind = new KeyMapping(key.description, key.initialType, key.key, TheMightyArchitect.NAME);
			if (!key.modifiable)
				continue;
			event.register(key.keybind);
		}
	}

	public static void handleKey(InputEvent.Key event) {
		handleInput(event.getAction(), k -> k.getKeybind()
			.matches(event.getKey(), event.getScanCode()));
	}

	public static void handleMouseButton(InputEvent.MouseButton event) {
		handleInput(event.getAction(), k -> k.getKeybind()
			.matchesMouse(event.getButton()));
	}

	private static void handleInput(int action, Predicate<Keybinds> filter) {
		if (action == InputConstants.REPEAT)
			return;
		Arrays.stream(values())
			.filter(filter)
			.flatMap(k -> k.callbacks.stream())
			.forEach(kl -> kl.keybindTriggered(action == InputConstants.PRESS));
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public boolean matches(int key) {
		return keybind.getKey()
			.getValue() == key;
	}

	public int getBoundCode() {
		return keybind.getKey()
			.getValue();
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.getWindow(), button) == 1;
	}

	public static boolean ctrlDown() {
		return Screen.hasControlDown();
	}

	public static boolean shiftDown() {
		return Screen.hasShiftDown();
	}

	public static boolean altDown() {
		return Screen.hasAltDown();
	}

}
