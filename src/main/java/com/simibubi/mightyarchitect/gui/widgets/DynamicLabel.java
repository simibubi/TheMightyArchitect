package com.simibubi.mightyarchitect.gui.widgets;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class DynamicLabel extends Gui {

	public String text;
	public int x, y;
	
	public DynamicLabel(int x, int y) {
		this.x = x; this.y = y;
		this.text = "Label";
	}
	
	public void draw(GuiScreen screen) {
		drawString(screen.mc.fontRenderer, text, x, y, 0xff_ff_ff);
	}
	
}
