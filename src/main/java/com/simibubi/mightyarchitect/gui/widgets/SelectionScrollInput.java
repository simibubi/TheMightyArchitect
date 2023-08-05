package com.simibubi.mightyarchitect.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SelectionScrollInput extends ScrollInput {

	protected List<String> options;
	protected Component scrollToSelect = Lang.text("Scroll to Select")
		.component();

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
		displayLabel.text = Lang.text(options.get(state))
			.component();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return super.mouseScrolled(mouseX, mouseY, -delta);
	}

	@Override
	protected void updateTooltip() {
		toolTip.clear();
		toolTip.add(title.plainCopy()
			.withStyle(ChatFormatting.BLUE));
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(Lang.empty()
					.plainCopy()
					.append("-> ")
					.append(options.get(i))
					.withStyle(ChatFormatting.WHITE));
			else
				toolTip.add(Lang.empty()
					.plainCopy()
					.append("> ")
					.append(options.get(i))
					.withStyle(ChatFormatting.GRAY));
		}
		toolTip.add(Lang.empty()
			.plainCopy()
			.append(scrollToSelect)
			.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
	}

}
