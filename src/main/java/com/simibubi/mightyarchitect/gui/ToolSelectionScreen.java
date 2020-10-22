package com.simibubi.mightyarchitect.gui;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class ToolSelectionScreen extends Screen {

	protected List<Tools> tools;
	protected Consumer<Tools> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<Tools> tools, Consumer<Tools> callback) {
		super(new StringTextComponent("Tool Selection"));
		this.client = Minecraft.getInstance();
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = 0;
		selection = 0;

		w = tools.size() * 50 + 30;
		h = 30;
	}

	public void cycle(int direction) {
		selection += (direction < 0) ? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
	}

	private void draw(MatrixStack ms, float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance()
			.getWindow();
		FontRenderer font = client.fontRenderer;

		int x = (mainWindow.getScaledWidth() - w) / 2 + 15;
		int y = 15;

		RenderSystem.pushMatrix();
		RenderSystem.translatef(0, 0, focused ? 100 : 0);

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
		RenderSystem.color4f(1, 1, 1, focused ? 7 / 8f : 1 / 2f);
		Minecraft.getInstance()
			.getTextureManager()
			.bindTexture(gray.location);
		float toolTipAlpha = yOffset / 10;

		// render main box
		drawTexture(ms, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

		// render tools
		List<String> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			Minecraft.getInstance()
				.getTextureManager()
				.bindTexture(gray.location);
			RenderSystem.color4f(.7f, .7f, .8f, toolTipAlpha);
			drawTexture(ms, x - 15, y + 30, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			RenderSystem.color4f(1, 1, 1, 1);

			if (toolTip.size() > 0)
				font.draw(ms, toolTip.get(0), x - 10, y + 35, 0xEEEEEE + stringAlphaComponent);
			if (toolTip.size() > 1)
				font.draw(ms, toolTip.get(1), x - 10, y + 47, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 2)
				font.draw(ms, toolTip.get(2), x - 10, y + 57, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 3)
				font.draw(ms, toolTip.get(3), x - 10, y + 69, 0xCCCCDD + stringAlphaComponent);
		}

		RenderSystem.color4f(1, 1, 1, 1);
		String translationKey = MightyClient.TOOL_MENU.getBoundKeyLocalizedText()
			.getString()
			.toUpperCase();
		int width = client.getWindow()
			.getScaledWidth();
		if (!focused)
			drawCenteredString(ms, client.fontRenderer, "Hold [" + translationKey + "] to focus", width / 2, y - 10,
				0xCCDDFF);
		else
			drawCenteredString(ms, client.fontRenderer, "[SCROLL] to Cycle", width / 2, y - 10, 0xCCDDFF);

		for (int i = 0; i < tools.size(); i++) {
			RenderSystem.pushMatrix();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				RenderSystem.translatef(0, -10, 0);
				drawCenteredString(ms, client.fontRenderer, tools.get(i)
					.getDisplayName(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			RenderSystem.color4f(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.draw(ms, this, x + i * 50 + 16, y + 12);
			RenderSystem.color4f(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.draw(ms, this, x + i * 50 + 16, y + 11);

			RenderSystem.popMatrix();
		}

		RenderSystem.popMatrix();
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(MatrixStack ms, float partialTicks) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		draw(ms, partialTicks);
	}

	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

}
