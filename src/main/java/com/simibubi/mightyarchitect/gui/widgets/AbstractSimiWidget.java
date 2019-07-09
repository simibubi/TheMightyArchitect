package com.simibubi.mightyarchitect.gui.widgets;

import net.minecraft.client.gui.widget.Widget;

public abstract class AbstractSimiWidget extends Widget {

	protected String toolTip;
	
	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, "");
	}
	
	public String getToolTip() {
		return toolTip;
	}

}
