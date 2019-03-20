package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.proxy.CombinedClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class GuiArchitectMenu extends GuiScreen {

	private Map<String, String> keybinds;
	private String title;
	private List<String> tooltip;
	private boolean focused;
	private boolean visible;

	private int menuWidth;
	private int menuHeight;

	private int targetY;
	private float movingY;

	public GuiArchitectMenu() {
		keybinds = new HashMap<>();
		tooltip = new ArrayList<>();
		title = "";
		focused = false;
		visible = false;
		movingY = 0;
		targetY = 0;
		adjustTarget();
	}

	public void updateContents() {
		int fontheight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

		// update tooltips and keybinds
		tooltip = ArchitectManager.getPhase().getPhaseHandler().getToolTip();
		keybinds = ArchitectManager.getKeybinds();
		title = ArchitectManager.getPhase().getDisplayTitle();

		menuWidth = 158;
		menuHeight = 4;

		menuHeight += 12; // title
		menuHeight += 4 + (keybinds.size() * fontheight); // keybinds

		menuHeight += 4;
		for (String s : tooltip) {
			int lines = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(s, menuWidth - 8).size();
			menuHeight += lines * fontheight + 2;
		}

		adjustTarget();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// FOCUSED
		super.drawScreen(mouseX, mouseY, partialTicks);
		draw(partialTicks);
	}

	public void drawPassive() {
		if (isFocused())
			return;

		// NOT FOCUSED
		draw(Minecraft.getMinecraft().getRenderPartialTicks());
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		if (keyCode == CombinedClientProxy.COMPOSE.getKeyCode()) {
			mc.displayGuiScreen(null);
			return;
		}
		
		if (ArchitectManager.handleMenuInput(keyCode, typedChar))
			mc.displayGuiScreen(null);
	}

	private void draw(float partialTicks) {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

		int x = res.getScaledWidth() - menuWidth - 10;
		int y = res.getScaledHeight() - menuHeight;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, yShift(partialTicks), 0);
		
		GuiResources gray = GuiResources.GRAY;
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1, 3 / 4f);

		Minecraft.getMinecraft().getTextureManager().bindTexture(gray.location);
		drawModalRectWithCustomSizedTexture(x, y, gray.startX, gray.startY, menuWidth, menuHeight, gray.width,
				gray.height);
		GlStateManager.color(1, 1, 1, 1);

		int yPos = y + 4;
		int xPos = x + 4;

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		if (!focused)
		font.drawString("Press " + Keyboard.getKeyName(CombinedClientProxy.COMPOSE.getKeyCode()) + " to focus", xPos,
				yPos - 14, 0xEEEEEE, true);
		font.drawString(title, xPos, yPos, 0xEEEEEE, false);

		yPos += 4;
		for (String key : keybinds.keySet()) {
			yPos += font.FONT_HEIGHT;
			font.drawString("[" + key + "] " + keybinds.get(key), xPos, yPos, 0xEEEEEE, false);
			font.drawString(">", xPos - 12, yPos, 0xCCDDFF, true);
		}

		yPos += 4;
		yPos += font.FONT_HEIGHT;
		for (String text : tooltip) {
			int lines = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, menuWidth -8).size();
			font.drawSplitString(text, xPos, yPos, menuWidth - 8, 0xEEEEEE);
			yPos += font.FONT_HEIGHT * lines + 2;
		}

		GlStateManager.popMatrix();
	}

	@Override
	public boolean doesGuiPauseGame() {
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
		targetY = visible ? (focused ? 0 : menuHeight - 14) : menuHeight + 20;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		adjustTarget();
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		setFocused(false);
	}

	private float yShift(float partialTicks) {
		return (movingY + (targetY - movingY) * 0.2f * partialTicks);
	}

	public void onClientTick() {
		if (movingY != targetY) {
			movingY += (targetY - movingY) * 0.2;
		}
	}

}
