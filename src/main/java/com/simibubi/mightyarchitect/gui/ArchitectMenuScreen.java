package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.ArchitectMenu;
import com.simibubi.mightyarchitect.control.ArchitectMenu.KeyBindList;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class ArchitectMenuScreen extends Screen {

	private KeyBindList keybinds;
	private String title;
	private List<String> tooltip;
	private boolean focused;
	private boolean visible;

	private int menuWidth;
	private int menuHeight;

	private int targetY;
	private float movingY;

	public ArchitectMenuScreen() {
		super(new StringTextComponent("Architect Menu"));
		keybinds = new KeyBindList();
		tooltip = new ArrayList<>();
		title = "";
		focused = false;
		visible = false;
		movingY = 0;
		targetY = 0;
		adjustTarget();
	}

	public void updateContents() {
		int fontheight = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;

		// update tooltips and keybinds
		tooltip = ArchitectManager.getPhase().getPhaseHandler().getToolTip();
		keybinds = ArchitectMenu.getKeybinds();
		title = ArchitectManager.getPhase().getDisplayTitle();

		menuWidth = 158;
		menuHeight = 4;

		menuHeight += 12; // title
		menuHeight += 4 + (keybinds.size() * fontheight); // keybinds

		menuHeight += 4;
		for (String s : tooltip) {
			int lines = Minecraft.getInstance().fontRenderer.listFormattedStringToWidth(s, menuWidth - 8).size();
			menuHeight += lines * fontheight + 2;
		}

		adjustTarget();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		// FOCUSED
		super.render(mouseX, mouseY, partialTicks);
		draw(partialTicks);
	}

	public void drawPassive() {
		if (isFocused())
			return;

		// NOT FOCUSED
		draw(Minecraft.getInstance().getRenderPartialTicks());
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
		
		if (keyCode == TheMightyArchitect.COMPOSE.getKey().getKeyCode()) {
			minecraft.displayGuiScreen(null);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if (ArchitectMenu.handleMenuInput(p_charTyped_1_)) {
			minecraft.displayGuiScreen(null);
			return true;			
		}
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}
	
	private void draw(float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance().mainWindow;

		int x = mainWindow.getScaledWidth() - menuWidth - 10;
		int y = mainWindow.getScaledHeight() - menuHeight;

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, yShift(partialTicks), 0);
		
		ScreenResources gray = ScreenResources.GRAY;
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, 3 / 4f);

		Minecraft.getInstance().getTextureManager().bindTexture(gray.location);
		blit(x, y, gray.startX, gray.startY, menuWidth, menuHeight, gray.width,
				gray.height);
		GlStateManager.color4f(1, 1, 1, 1);

		int yPos = y + 4;
		int xPos = x + 4;

		FontRenderer font = Minecraft.getInstance().fontRenderer;
		if (!focused)
		font.drawString("Press " + TheMightyArchitect.COMPOSE.getLocalizedName().toUpperCase() + " to focus", xPos,
				yPos - 14, 0xEEEEEE);
		font.drawString(title, xPos, yPos, 0xEEEEEE);

		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += font.FONT_HEIGHT / 2;
				continue;
			}
			
			yPos += font.FONT_HEIGHT;
			font.drawString("[" + key + "] " + keybinds.get(key), xPos, yPos, 0xEEEEEE);
			font.drawString(">", xPos - 12, yPos, 0xCCDDFF);
		}

		yPos += 4;
		yPos += font.FONT_HEIGHT;
		for (String text : tooltip) {
			int lines = Minecraft.getInstance().fontRenderer.listFormattedStringToWidth(text, menuWidth -8).size();
			font.drawSplitString(text, xPos, yPos, menuWidth - 8, 0xEEEEEE);
			yPos += font.FONT_HEIGHT * lines + 2;
		}

		GlStateManager.popMatrix();
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
		targetY = visible ? (focused ? 0 : menuHeight - 14) : menuHeight + 20;
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

	private float yShift(float partialTicks) {
		return (movingY + (targetY - movingY) * 0.2f * partialTicks);
	}

	public void onClientTick() {
		if (movingY != targetY) {
			movingY += (targetY - movingY) * 0.2;
		}
	}

}
