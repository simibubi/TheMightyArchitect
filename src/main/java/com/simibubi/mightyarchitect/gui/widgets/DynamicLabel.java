package com.simibubi.mightyarchitect.gui.widgets;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;

public class DynamicLabel extends AbstractGui {

	public String text;
	public int x, y;
	
	public DynamicLabel(int x, int y) {
		this.x = x; this.y = y;
		this.text = "Label";
	}
	
	public void draw(Screen screen) {
		drawString(screen.getMinecraft().fontRenderer, text, x, y, 0xff_ff_ff);
	}
	
}
