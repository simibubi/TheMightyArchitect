package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.Keybinds;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.ArchitectMenu;
import com.simibubi.mightyarchitect.control.ArchitectMenu.KeyBindList;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

public class ArchitectMenuScreen extends Screen {

	private KeyBindList keybinds;
	private String title;
	private List<String> tooltip;
	private boolean focused;
	private boolean visible;

	private int menuWidth;
	private int menuHeight;

	private LerpedFloat animation;

	public ArchitectMenuScreen() {
		super(Lang.text("Architect Menu")
			.component());
		keybinds = new KeyBindList();
		tooltip = new ArrayList<>();
		title = "";
		focused = false;
		visible = false;
		animation = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0.2f, Chaser.EXP);
		adjustTarget();
	}

	public void updateContents() {
		int textRendererheight = Minecraft.getInstance().font.lineHeight;

		// update tooltips and keybinds
		tooltip = ArchitectManager.getPhase()
			.getPhaseHandler()
			.getToolTip();
		keybinds = ArchitectMenu.getKeybinds();
		title = ArchitectManager.getPhase()
			.getDisplayTitle();

		menuWidth = 158;
		menuHeight = 4;

		menuHeight += 12; // title
		menuHeight += 4 + (keybinds.size() * textRendererheight); // keybinds

		menuHeight += 4;
		for (String s : tooltip)
			menuHeight += Minecraft.getInstance().font.wordWrapHeight(s, menuWidth - 8) + 2;

		adjustTarget();
	}

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		// FOCUSED
		super.render(ms, mouseX, mouseY, partialTicks);
		draw(ms, partialTicks);
	}

	public void drawPassive() {
		if (isFocused())
			return;

		// NOT FOCUSED
		draw(new PoseStack(), Minecraft.getInstance()
			.getFrameTime());
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
		boolean hideOnClose =
			ArchitectManager.inPhase(ArchitectPhases.Empty) || ArchitectManager.inPhase(ArchitectPhases.Paused);

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}

		if (Keybinds.ACTIVATE.matches(keyCode)) {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		boolean hideOnClose =
			ArchitectManager.inPhase(ArchitectPhases.Empty) || ArchitectManager.inPhase(ArchitectPhases.Paused);
		if (ArchitectMenu.handleMenuInput(p_charTyped_1_)) {
			if (ArchitectManager.inPhase(ArchitectPhases.Paused))
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}
		if (p_charTyped_1_ == 'e') {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}

		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	private void draw(PoseStack ms, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Window mainWindow = mc.getWindow();
		partialTicks = mc.getFrameTime();

		int x = mainWindow.getGuiScaledWidth() - menuWidth - 10;
		int y = mainWindow.getGuiScaledHeight() - menuHeight;

		int mouseX = (int) (mc.mouseHandler.xpos() / mainWindow.getGuiScale());
		int mouseY = (int) (mc.mouseHandler.ypos() / mainWindow.getGuiScale());

		boolean sideways = false;
		if ((mainWindow.getGuiScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			y -= 24;
		}

		ms.pushPose();
		float shift = animation.getValue(partialTicks);
		float sidewaysShift =
			shift * ((float) menuWidth / (float) menuHeight) + (!focused ? 40 + menuHeight / 4f : 0) + 8;
		ms.translate(sideways ? sidewaysShift : 0, sideways ? 0 : shift, 0);
		mouseX -= sideways ? sidewaysShift : 0;
		mouseY -= sideways ? 0 : shift;

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 3 / 4f);

		RenderSystem.setShaderTexture(0, gray.location);
		RenderSystem.enableTexture();
		blit(ms, x, y, gray.startX, gray.startY, menuWidth, menuHeight, gray.width, gray.height);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		int yPos = y + 4;
		int xPos = x + 4;

		Font textRenderer = mc.font;
		String compose = Keybinds.ACTIVATE.getBoundKey();
		if (!focused) {
			if (sideways) {
				if (visible) {
					String string = "Press " + compose.toUpperCase() + " for Menu";
					textRenderer.drawShadow(ms, string,
						mainWindow.getGuiScaledWidth() - textRenderer.width(string) - 15 - sidewaysShift, yPos - 14,
						0xEEEEEE);
				}
			} else {
				textRenderer.drawShadow(ms, "Press " + compose.toUpperCase() + " to focus", xPos, yPos - 14, 0xEEEEEE);
			}
		} else {
			String string = "Press " + compose + " to close";
			textRenderer.drawShadow(ms, string,
				sideways
					? Math.min(xPos, mainWindow.getGuiScaledWidth() - textRenderer.width(string) - 15 - sidewaysShift)
					: xPos,
				yPos - 14, 0xDDDDDD);
		}
		textRenderer.drawShadow(ms, title, xPos, yPos, 0xEEEEEE);

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth && focused;

		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += textRenderer.lineHeight / 2;
				continue;
			}

			yPos += textRenderer.lineHeight;
			int color =
				hoveredHorizontally && yPos < mouseY && mouseY <= yPos + textRenderer.lineHeight ? 0xFFFFFF : 0xCCDDFF;
			textRenderer.drawShadow(ms, "[" + key + "] " + keybinds.get(key), xPos, yPos, color);
			textRenderer.drawShadow(ms, ">", xPos - 12, yPos, color);
		}

		yPos += 4;
		yPos += textRenderer.lineHeight;
		for (String text : tooltip) {
			int height = mc.font.wordWrapHeight(text, menuWidth - 8);
			int lineY = yPos;
			for (FormattedCharSequence iro : textRenderer.split(Lang.text(text)
				.component(), menuWidth - 8)) {
				textRenderer.draw(ms, iro, xPos, lineY, 0xEEEEEE);
				lineY += textRenderer.lineHeight;
			}
			yPos += height + 2;
		}

		ms.popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !visible || !focused)
			return super.mouseClicked(mouseX, mouseY, button);

		Window mainWindow = Minecraft.getInstance()
			.getWindow();
		int x = mainWindow.getGuiScaledWidth() - menuWidth - 10;
		int y = mainWindow.getGuiScaledHeight() - menuHeight;

		boolean sideways = false;
		if ((mainWindow.getGuiScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			mouseY += 24;
		}

		float shift = animation.getValue();
		mouseX -= sideways ? shift * 2 : 0;
		mouseY -= sideways ? 0 : shift;

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth;

		int yPos = y + 4;
		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += font.lineHeight / 2;
				continue;
			}

			yPos += font.lineHeight;
			if (hoveredHorizontally && yPos < mouseY && mouseY <= yPos + font.lineHeight) {
				charTyped(key.toLowerCase()
					.charAt(0), GLFW.GLFW_PRESS);
			}
		}

		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
		adjustTarget();
	}

	protected void adjustTarget() {
		animation.updateChaseTarget(visible ? (focused ? 0 : menuHeight - 14) : menuHeight + 20);
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		adjustTarget();
	}

	@Override
	public void removed() {
		super.removed();
		setFocused(false);
	}

	public void onClientTick() {
		animation.tickChaser();
	}

}
