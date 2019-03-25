package com.simibubi.mightyarchitect.gui.widgets;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class ScrollArea extends Gui {

	public interface IScrollAction {
		public void onScroll(int position);
	}

	public interface ICancelableScrollAction extends IScrollAction {
		public void onScroll(int position);

		public boolean canScroll(int position);
	}

	private int x, y, width, height;
	private IScrollAction action;
	public boolean enabled;
	private int currentState;
	private Optional<List<String>> tooltipContent;
	private List<String> tooltip;
	private String title = "Choose an option";
	private int min, max;
	private boolean limitless;
	private boolean numeric;

	public ScrollArea(List<String> options, IScrollAction action) {
		this(0, options.size(), action);
		this.tooltipContent = Optional.of(options);
		updateTooltip();
	}

	public ScrollArea(int min, int max, IScrollAction action) {
		this(action);
		this.limitless = false;
		this.min = min;
		this.max = max;
	}

	public ScrollArea(IScrollAction action) {
		this.enabled = true;
		this.action = action;
		this.tooltipContent = Optional.absent();
		this.limitless = true;
		this.numeric = false;
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setState(int state) {
		currentState = state;
		updateTooltip();
	}

	public int getState() {
		return currentState;
	}

	public boolean isHovered(int x, int y) {
		return (x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height);
	}

	public void tryScroll(int mouseX, int mouseY, int amount) {
		if (enabled && isHovered(mouseX, mouseY)) {
			scroll(numeric? -amount : amount);
		}
	}
	
	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	private void scroll(int amount) {
		if (enabled) {

			if (limitless) {
				if (!(action instanceof ICancelableScrollAction)
						|| ((ICancelableScrollAction) action).canScroll(amount))
					action.onScroll(amount);
				return;
			}

			if (!(action instanceof ICancelableScrollAction)
					|| ((ICancelableScrollAction) action).canScroll(currentState + amount)) {
				currentState += amount;
				if (currentState < min)
					currentState = min;
				if (currentState >= max)
					currentState = max - 1;
				updateTooltip();
				action.onScroll(currentState);
			}
		}
	}

	public void draw(GuiScreen screen, int mouseX, int mouseY) {
		GlStateManager.pushAttrib();
		if (enabled && isHovered(mouseX, mouseY)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(mouseX, mouseY,0);
			if (tooltipContent.isPresent())
				screen.drawHoveringText(getToolTip(), 0, 0);
			else
				screen.drawHoveringText(ChatFormatting.BLUE + title, 0, 0);
			GlStateManager.popMatrix();
		}

		GlStateManager.popAttrib();
	}

	public List<String> getToolTip() {
		return tooltip;
	}

	public void setTitle(String title) {
		this.title = title;
		updateTooltip();
	}

	private void updateTooltip() {
		tooltip = new LinkedList<>();
		tooltip.add(ChatFormatting.BLUE + title);

		if (tooltipContent.isPresent()) {
			for (int i = min; i < max; i++) {
				StringBuilder result = new StringBuilder();
				if (i == currentState)
					result.append(ChatFormatting.WHITE).append("-> ").append(tooltipContent.get().get(i));
				else
					result.append(ChatFormatting.GRAY).append("> ").append(tooltipContent.get().get(i));
				tooltip.add(result.toString());
			}

		}
	}

}
