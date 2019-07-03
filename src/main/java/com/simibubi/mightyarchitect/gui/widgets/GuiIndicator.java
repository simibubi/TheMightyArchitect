package com.simibubi.mightyarchitect.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class GuiIndicator extends AbstractGui {
	
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
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(toDraw.location);
		blit(x, y, toDraw.startX, toDraw.startY, toDraw.width, toDraw.height);
	}
	
}
