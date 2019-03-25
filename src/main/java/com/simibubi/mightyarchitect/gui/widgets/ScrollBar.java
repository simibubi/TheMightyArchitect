package com.simibubi.mightyarchitect.gui.widgets;

import com.simibubi.mightyarchitect.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ScrollBar extends Gui {

	protected int x, y;
	protected int span;
	protected float position;
	protected float scale;
	protected boolean active;

	public ScrollBar(int x, int span) {
		this.x = x;
		this.y = (-GuiResources.SCROLLBAR_AXIS.height
				+ new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight()) / 2;
		this.span = span;
		scale = span / 200f;
		position = 1;
		active = scale > 1;
	}

	public void setPosition(float position) {
		this.position = position;
	}

	public void render(GuiScreen screen) {
		if (!active)
			return;
		GlStateManager.color(1, 1, 1);

		GuiResources.SCROLLBAR_AXIS.draw(screen, x, y);

		int widgetX = x - 6;
		int barSize = (int) (200f / span * 256);
		int barY = y + (int) ((1 - position) * (256 - barSize));
		
		GuiResources.SCROLLBAR_CAP.draw(screen, widgetX, barY);
		GuiResources.SCROLLBAR_CAP.draw(screen, widgetX, barY + barSize - 6);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(widgetX, barY + 6, 0);
		GlStateManager.scale(1, (barSize - 12) / 16f, 1);
		GuiResources.SCROLLBAR_BACKGROUND.draw(screen, 0, 0);
		GlStateManager.popMatrix();
	}

	public float getYShift() {
		if (!active)
			return 0;
		return (-0.5f + position) * (span - 200);
	}

	public void tryScroll(int mouseX, int mouseY, int amount) {
		if (mouseX > x && mouseX < x + 32 && mouseY > y && mouseY < y + 256) {
			position -= (1 / scale) * 0.2f * amount;
			position = position > 1 ? 1 : position < 0 ? 0 : position;
		}
	}

}
