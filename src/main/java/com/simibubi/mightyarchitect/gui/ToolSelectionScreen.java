package com.simibubi.mightyarchitect.gui;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.Keybinds;
import com.simibubi.mightyarchitect.control.compose.planner.Tools;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public class ToolSelectionScreen extends Screen {

	protected List<Tools> tools;
	protected Consumer<Tools> callback;
	public boolean focused;
	private LerpedFloat yOffset;
	protected int selection;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<Tools> tools, Consumer<Tools> callback) {
		super(Lang.text("Tool Selection")
			.component());
		this.minecraft = Minecraft.getInstance();
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .1f, Chaser.EXP);
		selection = 0;

		w = tools.size() * 50 + 30;
		h = 30;
	}

	public void cycle(int direction) {
		selection += (direction < 0) ? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
	}

	public void select(int index) {
		selection = Mth.clamp(index, 0, tools.size() - 1);
	}

	private void draw(GuiGraphics graphics, float partialTicks) {
		Window mainWindow = Minecraft.getInstance()
			.getWindow();
		Font font = minecraft.font;
		PoseStack ms = graphics.pose();

		int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
		int y = 15;

		ms.pushPose();
		ms.translate(0, 0, focused ? 100 : 0);

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1, 1, 1, focused ? 7 / 8f : 1 / 2f);
		float toolTipAlpha = yOffset.getValue(partialTicks) / 10;

		// render main box
		graphics.blit(gray.location, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

		// render tools
		List<String> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			RenderSystem.setShaderColor(.7f, .7f, .8f, toolTipAlpha);
			graphics.blit(gray.location, x - 15, y + 30, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			if (toolTip.size() > 0)
				graphics.drawString(font, toolTip.get(0), x - 10, y + 35, 0xEEEEEE + stringAlphaComponent, false);
			if (toolTip.size() > 1)
				graphics.drawString(font, toolTip.get(1), x - 10, y + 47, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 2)
				graphics.drawString(font, toolTip.get(2), x - 10, y + 57, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 3)
				graphics.drawString(font, toolTip.get(3), x - 10, y + 69, 0xCCCCDD + stringAlphaComponent, false);
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);
		String translationKey = Keybinds.FOCUL_TOOL_MENU.getBoundKey();
		int width = minecraft.getWindow()
			.getGuiScaledWidth();
		if (!focused)
			graphics.drawCenteredString(minecraft.font, "Hold [" + translationKey + "] to focus", width / 2, y - 10,
				0xCCDDFF);
		else
			graphics.drawCenteredString(minecraft.font, "[SCROLL] to Cycle", width / 2, y - 10, 0xCCDDFF);

		for (int i = 0; i < tools.size(); i++) {
			ms.pushPose();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				ms.translate(0, -10, 0);
				graphics.drawCenteredString(minecraft.font, tools.get(i)
					.getDisplayName(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			RenderSystem.setShaderColor(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.draw(graphics, x + i * 50 + 16, y + 12);
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.draw(graphics, x + i * 50 + 16, y + 11);

			if (focused && i != selection) {
				KeyMapping keyMapping = minecraft.options.keyHotbarSlots[i];
				graphics.drawCenteredString(minecraft.font, "[" + keyMapping.getTranslatedKeyMessage()
					.getString() + "]", x + i * 50 + 24, y + 3, 0xCCDDFF);
			}

			ms.popPose();
		}

		ms.popPose();
	}

	public void update() {
		yOffset.updateChaseTarget(focused ? 10 : 0);
		yOffset.tickChaser();
	}

	public void renderPassive(GuiGraphics graphics, float partialTicks) {
		if (Minecraft.getInstance().screen != null)
			return;
		draw(graphics, partialTicks);
	}

	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

}
