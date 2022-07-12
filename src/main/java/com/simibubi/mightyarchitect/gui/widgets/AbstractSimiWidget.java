package com.simibubi.mightyarchitect.gui.widgets;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class AbstractSimiWidget extends AbstractWidget {

	protected List<Component> toolTip;
	
	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
		toolTip = new LinkedList<>();
	}
	
	public List<Component> getToolTip() {
		return toolTip;
	}
	
	@Override
	public void renderButton(PoseStack matrixStack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}

}
