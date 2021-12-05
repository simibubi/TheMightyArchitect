package com.simibubi.mightyarchitect.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.gui.ScreenResources;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class Indicator extends AbstractSimiWidget {
	
	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}
	
	public State state;
	
	public Indicator(int x, int y, String tooltip) {
		this(x, y, new TextComponent(tooltip));
	}
	
	public Indicator(int x, int y, Component tooltip) {
		super(x, y, ScreenResources.INDICATOR.width, ScreenResources.INDICATOR.height);
		this.toolTip = ImmutableList.of(tooltip);
		this.state = State.OFF;
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks ) {
		ScreenResources toDraw;
		switch(state) {
			case ON: toDraw = ScreenResources.INDICATOR_WHITE; break;
			case OFF: toDraw = ScreenResources.INDICATOR; break;
			case RED: toDraw = ScreenResources.INDICATOR_RED; break;
			case YELLOW: toDraw = ScreenResources.INDICATOR_YELLOW; break;
			case GREEN: toDraw = ScreenResources.INDICATOR_GREEN; break;
			default: toDraw = ScreenResources.INDICATOR; break;
		}
		toDraw.draw(matrixStack, this, x, y);
	}
	
}
