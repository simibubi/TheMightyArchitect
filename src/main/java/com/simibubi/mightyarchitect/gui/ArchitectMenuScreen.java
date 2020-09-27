package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.ArchitectMenu;
import com.simibubi.mightyarchitect.control.ArchitectMenu.KeyBindList;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;

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
		boolean hideOnClose = ArchitectManager.inPhase(ArchitectPhases.Empty)
				|| ArchitectManager.inPhase(ArchitectPhases.Paused);

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (hideOnClose)
				setVisible(false);
			minecraft.displayGuiScreen(null);
			return true;
		}

		if (keyCode == MightyClient.COMPOSE.getKey().getKeyCode()) {
			if (hideOnClose)
				setVisible(false);
			minecraft.displayGuiScreen(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		boolean hideOnClose = ArchitectManager.inPhase(ArchitectPhases.Empty)
				|| ArchitectManager.inPhase(ArchitectPhases.Paused);
		if (ArchitectMenu.handleMenuInput(p_charTyped_1_)) {
			if (ArchitectManager.inPhase(ArchitectPhases.Paused))
				setVisible(false);
			minecraft.displayGuiScreen(null);
			return true;
		}
		if (p_charTyped_1_ == 'e') {
			if (hideOnClose)
				setVisible(false);
			minecraft.displayGuiScreen(null);
			return true;
		}

		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	private void draw(float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getScaledWidth() - menuWidth - 10;
		int y = mainWindow.getScaledHeight() - menuHeight;

		int mouseX = (int) (Minecraft.getInstance().mouseHelper.getMouseX() / mainWindow.getGuiScaleFactor());
		int mouseY = (int) (Minecraft.getInstance().mouseHelper.getMouseY() / mainWindow.getGuiScaleFactor());

		boolean sideways = false;
		if ((mainWindow.getScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			y -= 24;
		}

		RenderSystem.pushMatrix();
		float shift = yShift(partialTicks);
		float sidewaysShift = shift * ((float) menuWidth / (float) menuHeight) + (!focused ? 40 + menuHeight / 4f : 0)
				+ 8;
		RenderSystem.translatef(sideways ? sidewaysShift : 0, sideways ? 0 : shift, 0);
		mouseX -= sideways ? sidewaysShift : 0;
		mouseY -= sideways ? 0 : shift;

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.color4f(1, 1, 1, 3 / 4f);

		Minecraft.getInstance().getTextureManager().bindTexture(gray.location);
		blit(x, y, gray.startX, gray.startY, menuWidth, menuHeight, gray.width, gray.height);
		RenderSystem.color4f(1, 1, 1, 1);

		int yPos = y + 4;
		int xPos = x + 4;

		FontRenderer font = Minecraft.getInstance().fontRenderer;
		if (!focused) {
			if (sideways) {
				if (visible) {
					String string = "Press " + MightyClient.COMPOSE.getLocalizedName().toUpperCase()
							+ " for Menu";
					font.drawStringWithShadow(string,
							mainWindow.getScaledWidth() - font.getStringWidth(string) - 15 - sidewaysShift, yPos - 14,
							0xEEEEEE);
				}
			} else {
				font.drawStringWithShadow(
						"Press " + MightyClient.COMPOSE.getLocalizedName().toUpperCase() + " to focus", xPos,
						yPos - 14, 0xEEEEEE);
			}
		} else {
			String string = "Press " + MightyClient.COMPOSE.getLocalizedName().toUpperCase() + " to close";
			font.drawStringWithShadow(string,
					sideways ? Math.min(xPos,
							mainWindow.getScaledWidth() - font.getStringWidth(string) - 15 - sidewaysShift) : xPos,
					yPos - 14, 0xDDDDDD);
		}
		font.drawStringWithShadow(title, xPos, yPos, 0xEEEEEE);

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth && focused;

		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += font.FONT_HEIGHT / 2;
				continue;
			}

			yPos += font.FONT_HEIGHT;
			int color = hoveredHorizontally && yPos < mouseY && mouseY <= yPos + font.FONT_HEIGHT ? 0xFFFFFF : 0xCCDDFF;
			font.drawStringWithShadow("[" + key + "] " + keybinds.get(key), xPos, yPos, color);
			font.drawStringWithShadow(">", xPos - 12, yPos, color);
		}

		yPos += 4;
		yPos += font.FONT_HEIGHT;
		for (String text : tooltip) {
			int lines = Minecraft.getInstance().fontRenderer.listFormattedStringToWidth(text, menuWidth - 8).size();
			font.drawSplitString(text, xPos, yPos, menuWidth - 8, 0xEEEEEE);
			yPos += font.FONT_HEIGHT * lines + 2;
		}

		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !visible || !focused)
			return super.mouseClicked(mouseX, mouseY, button);

		MainWindow mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getScaledWidth() - menuWidth - 10;
		int y = mainWindow.getScaledHeight() - menuHeight;

		boolean sideways = false;
		if ((mainWindow.getScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			mouseY += 24;
		}

		float shift = yShift(0);
		mouseX -= sideways ? shift * 2 : 0;
		mouseY -= sideways ? 0 : shift;

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth;

		int yPos = y + 4;
		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += font.FONT_HEIGHT / 2;
				continue;
			}

			yPos += font.FONT_HEIGHT;
			if (hoveredHorizontally && yPos < mouseY && mouseY <= yPos + font.FONT_HEIGHT) {
				charTyped(key.toLowerCase().charAt(0), GLFW.GLFW_PRESS);
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
