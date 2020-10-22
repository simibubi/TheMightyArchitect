package com.simibubi.mightyarchitect.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SelectionScrollInput extends ScrollInput {

	protected List<String> options;
	protected ITextComponent scrollToSelect = new StringTextComponent("Scroll to Select");

	public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
	}

	public ScrollInput forOptions(List<String> options) {
		this.options = options;
		this.max = options.size();
		updateTooltip();
		return this;
	}

	@Override
	protected void writeToLabel() {
		displayLabel.text = new StringTextComponent(options.get(state));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return super.mouseScrolled(mouseX, mouseY, -delta);
	}

	@Override
	protected void updateTooltip() {
		toolTip.clear();
		toolTip.add(title.copy()
			.formatted(TextFormatting.BLUE));
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(StringTextComponent.EMPTY.copy()
					.append("-> ")
					.append(options.get(i))
					.formatted(TextFormatting.WHITE));
			else
				toolTip.add(StringTextComponent.EMPTY.copy()
					.append("> ")
					.append(options.get(i))
					.formatted(TextFormatting.GRAY));
		}
		toolTip.add(StringTextComponent.EMPTY.copy()
			.append(scrollToSelect)
			.formatted(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
	}

}
