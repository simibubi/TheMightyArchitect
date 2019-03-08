package com.simibubi.mightyarchitect.gui.widgets;

import com.simibubi.mightyarchitect.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class GuiIndicator extends Gui {
	
	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}
	
	public State state;
	public boolean hovered;
	public String tooltip;
	private int x, y;
	private int width, height;
	
	public GuiIndicator( int x, int y, String tooltip ) {
		this.x = x;
		this.y = y;
		this.width = GuiResources.INDICATOR.width;
		this.height = GuiResources.INDICATOR.height;
		this.tooltip = tooltip;
		this.state = State.OFF;
	}
	
	public void render( Minecraft mc, int mouseX, int mouseY ) {
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		GuiResources toDraw;
		switch(state) {
			case ON: toDraw = GuiResources.INDICATOR_WHITE; break;
			case OFF: toDraw = GuiResources.INDICATOR; break;
			case RED: toDraw = GuiResources.INDICATOR_RED; break;
			case YELLOW: toDraw = GuiResources.INDICATOR_YELLOW; break;
			case GREEN: toDraw = GuiResources.INDICATOR_GREEN; break;
			default: toDraw = GuiResources.INDICATOR; break;
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(toDraw.location);
		drawTexturedModalRect(x, y, toDraw.startX, toDraw.startY, toDraw.width, toDraw.height);
	}
	
}
