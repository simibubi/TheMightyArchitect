package com.simibubi.mightyarchitect.gui;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class ToolSelectionScreen extends Screen {

	protected List<Tools> tools;
	protected Consumer<Tools> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<Tools> tools, Consumer<Tools> callback) {
		super(new TextComponent("Tool Selection"));
		this.minecraft = Minecraft.getInstance();
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

	private void draw(PoseStack ms, float partialTicks) {
		Window mainWindow = Minecraft.getInstance()
			.getWindow();
		Font font = minecraft.font;

		int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
		int y = 15;

		ms.pushPose();
		ms.translate(0, 0, focused ? 100 : 0);

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableTexture();
		RenderSystem.setShaderColor(1, 1, 1, focused ? 7 / 8f : 1 / 2f);
		RenderSystem.setShaderTexture(0, gray.location);
		float toolTipAlpha = yOffset / 10;

		// render main box
		blit(ms, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

		// render tools
		List<String> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			RenderSystem.setShaderTexture(0, gray.location);
			RenderSystem.setShaderColor(.7f, .7f, .8f, toolTipAlpha);
			blit(ms, x - 15, y + 30, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			if (toolTip.size() > 0)
				font.draw(ms, toolTip.get(0), x - 10, y + 35, 0xEEEEEE + stringAlphaComponent);
			if (toolTip.size() > 1)
				font.draw(ms, toolTip.get(1), x - 10, y + 47, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 2)
				font.draw(ms, toolTip.get(2), x - 10, y + 57, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 3)
				font.draw(ms, toolTip.get(3), x - 10, y + 69, 0xCCCCDD + stringAlphaComponent);
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);
		String translationKey = MightyClient.TOOL_MENU.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
		int width = minecraft.getWindow()
			.getGuiScaledWidth();
		if (!focused)
			drawCenteredString(ms, minecraft.font, "Hold [" + translationKey + "] to focus", width / 2, y - 10,
				0xCCDDFF);
		else
			drawCenteredString(ms, minecraft.font, "[SCROLL] to Cycle", width / 2, y - 10, 0xCCDDFF);

		for (int i = 0; i < tools.size(); i++) {
			ms.pushPose();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				ms.translate(0, -10, 0);
				drawCenteredString(ms, minecraft.font, tools.get(i)
					.getDisplayName(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			RenderSystem.setShaderColor(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.draw(ms, this, x + i * 50 + 16, y + 12);
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.draw(ms, this, x + i * 50 + 16, y + 11);

			ms.popPose();
		}

		ms.popPose();
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(PoseStack ms, float partialTicks) {
		if (Minecraft.getInstance().screen != null)
			return;
		draw(ms, partialTicks);
	}

	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

}
