package com.simibubi.mightyarchitect.gui.widgets;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiWidget extends AbstractWidget {

	protected List<Component> toolTip;

	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, Lang.empty());
		toolTip = new LinkedList<>();
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

}
