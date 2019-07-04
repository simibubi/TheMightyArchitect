package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.StringTextComponent;

public class GuiDesignExporter extends Screen {

	public GuiDesignExporter() {
		super(new StringTextComponent("Design Exporter"));
	}

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;

	private List<ScrollArea> scrollAreas;

	private ScrollArea scrollAreaLayer;
	private ScrollArea scrollAreaType;
	private ScrollArea scrollAreaAdditionalData;

	private DynamicLabel labelTheme;
	private DynamicLabel labelLayer;
	private DynamicLabel labelType;
	private DynamicLabel labelAdditionalData;

	private String additionalDataKey;
	private int additionalDataValue;
	private float animationProgress;

	@Override
	public void init() {
		super.init();
		animationProgress = 0;
		xSize = GuiResources.EXPORTER.width + 100;
		ySize = GuiResources.EXPORTER.height + 50;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		DesignTheme theme = DesignExporter.theme;
		DesignLayer layer = DesignExporter.layer;
		DesignType type = DesignExporter.type;

		additionalDataValue = DesignExporter.designParameter;

		labelTheme = new DynamicLabel(xTopLeft + 96, yTopLeft + 28);
		labelLayer = new DynamicLabel(xTopLeft + 96, yTopLeft + 48);
		labelType = new DynamicLabel(xTopLeft + 96, yTopLeft + 68);
		labelAdditionalData = new DynamicLabel(xTopLeft + 96, yTopLeft + 88);

		scrollAreas = new LinkedList<>();
		additionalDataKey = "";
		initScrollAreas(theme, layer, type);
	}

	private void initScrollAreas(DesignTheme theme, DesignLayer layer, DesignType type) {
		scrollAreas.clear();

		List<DesignLayer> layers = theme.getLayers();

		if (!layers.contains(layer))
			layer = DesignLayer.Regular;

		List<String> layerOptions = new ArrayList<>();
		layers.forEach(l -> layerOptions.add(l.getDisplayName()));

		scrollAreaLayer = new ScrollArea(layerOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelLayer.text = layerOptions.get(position);
				initTypeScrollArea(theme, layers.get(position), DesignExporter.type);
			}
		});
		scrollAreaLayer.setBounds(xTopLeft + 93, yTopLeft + 45, 90, 14);
		scrollAreaLayer.setTitle("Style Layer");
		scrollAreaLayer.setState(layers.indexOf(layer));
		labelLayer.text = layer.getDisplayName();
		scrollAreas.add(scrollAreaLayer);

		initTypeScrollArea(theme, layer, type);

		labelTheme.text = theme.getDisplayName();
	}

	protected void initTypeScrollArea(DesignTheme theme, DesignLayer layer, DesignType type) {
		List<DesignType> types = new ArrayList<>(theme.getTypes());

		// Roofs only in Roofing layer and vice versa
		if (layer == DesignLayer.Roofing) {
			types.retainAll(DesignType.roofTypes());
		} else {
			types.removeAll(DesignType.roofTypes());
		}

		// Fallback if previous type is not selectable anymore
		if (!types.contains(type)) {
			type = DesignType.WALL;
			if (layer == DesignLayer.Roofing) {
				for (DesignType dt : DesignType.roofTypes()) {
					if (types.contains(dt)) {
						type = dt;
						break;
					}
				}
			}
		}

		// Prepare options
		List<String> typeOptions = new ArrayList<>();
		types.forEach(t -> typeOptions.add(t.getDisplayName()));

		if (scrollAreas.contains(scrollAreaType))
			scrollAreas.remove(scrollAreaType);

		scrollAreaType = new ScrollArea(typeOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelType.text = typeOptions.get(position);
				DesignExporter.type = types.get(position);
				initAdditionalDataScrollArea(types.get(position));
			}
		});
		scrollAreaType.setBounds(xTopLeft + 93, yTopLeft + 65, 90, 14);
		scrollAreaType.setTitle("Design Type");
		scrollAreaType.setState(types.indexOf(type));
		labelType.text = type.getDisplayName();
		scrollAreas.add(scrollAreaType);

		initAdditionalDataScrollArea(type);
	}

	private void initAdditionalDataScrollArea(DesignType type) {
		if (type.hasAdditionalData()) {

			additionalDataKey = type.getAdditionalDataName();

			if (type.hasSizeData()) {

				if (type == DesignType.ROOF) {
					if (additionalDataValue % 2 == 0)
						additionalDataValue++;
				}
				if (additionalDataValue < type.getMinSize())
					additionalDataValue = type.getMinSize();
				if (additionalDataValue > type.getMaxSize())
					additionalDataValue = type.getMaxSize();
				labelAdditionalData.text = additionalDataValue + "m";

				if (type == DesignType.ROOF) {
					int min = (type.getMinSize() - 1) / 2;
					int max = (type.getMaxSize() - 1) / 2;

					scrollAreaAdditionalData = new ScrollArea(min, max + 1, new IScrollAction() {
						@Override
						public void onScroll(int position) {
							additionalDataValue = position * 2 + 1;
							labelAdditionalData.text = additionalDataValue + "m";
						}
					});
					scrollAreaAdditionalData.setState((additionalDataValue - 1) / 2);

				} else {
					int min = type.getMinSize();
					int max = type.getMaxSize();

					scrollAreaAdditionalData = new ScrollArea(min, max + 1, new IScrollAction() {

						@Override
						public void onScroll(int position) {
							additionalDataValue = position;
							labelAdditionalData.text = position + "m";
						}

					});
					scrollAreaAdditionalData.setState(additionalDataValue);
				}

				scrollAreaAdditionalData.setNumeric(true);

			} else if (type.hasSubtypes()) {
				if (additionalDataValue == -1)
					additionalDataValue = 0;

				List<String> subtypeOptions = type.getSubtypeOptions();
				if (additionalDataValue >= subtypeOptions.size())
					additionalDataValue = 0;

				labelAdditionalData.text = subtypeOptions.get(additionalDataValue);
				scrollAreaAdditionalData = new ScrollArea(subtypeOptions, new IScrollAction() {
					@Override
					public void onScroll(int position) {
						additionalDataValue = position;
						labelAdditionalData.text = subtypeOptions.get(position);
					}
				});
				scrollAreaAdditionalData.setNumeric(false);
				scrollAreaAdditionalData.setState(additionalDataValue);
			}

			scrollAreaAdditionalData.setTitle(additionalDataKey);
			scrollAreaAdditionalData.setBounds(xTopLeft + 93, yTopLeft + 85, 90, 14);

		} else {

			additionalDataValue = -1;
			additionalDataKey = "";
			labelAdditionalData.text = "";
			scrollAreaAdditionalData = null;

		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		GuiResources.EXPORTER.draw(this, xTopLeft, yTopLeft);
		super.render(mouseX, mouseY, partialTicks);

		RenderHelper.enableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translatef((this.width - this.xSize) / 2 + 250, 280, 100);
		GlStateManager.rotatef(-30, .4f, 0, -.2f);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scalef(300, -300, 300);
		itemRenderer.renderItem(minecraft.player.getHeldItemMainhand(),
				itemRenderer.getModelWithOverrides(minecraft.player.getHeldItemMainhand()));
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		animationProgress++;

		int color = GuiResources.FONT_COLOR;
		font.drawString("Export custom Designs", xTopLeft + 10, yTopLeft + 10, color);

		font.drawString("Theme", xTopLeft + 10, yTopLeft + 28, color);
		font.drawString("Building Layer", xTopLeft + 10, yTopLeft + 48, color);
		font.drawString("Design Type", xTopLeft + 10, yTopLeft + 68, color);
		font.drawString(additionalDataKey, xTopLeft + 10, yTopLeft + 88, color);

		labelTheme.draw(this);
		labelLayer.draw(this);
		labelType.draw(this);
		labelAdditionalData.draw(this);

		scrollAreas.forEach(area -> area.draw(this, mouseX, mouseY));
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.draw(this, mouseX, mouseY);

	}

	@Override
	public void removed() {
		DesignTheme theme = DesignExporter.theme;
		DesignExporter.layer = theme.getLayers().get(scrollAreaLayer.getState());

		List<DesignType> types = new ArrayList<>(theme.getTypes());

		// Roofs only in Roofing layer and vice versa
		if (DesignExporter.layer == DesignLayer.Roofing) {
			types.retainAll(DesignType.roofTypes());
		} else {
			types.removeAll(DesignType.roofTypes());
		}

		DesignExporter.type = types.get(scrollAreaType.getState());
		DesignExporter.designParameter = additionalDataValue;
		PhaseEditTheme.setVisualization(PhaseEditTheme.selectedDesign);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.LSHIFT)) ? 5 : 1);
		scrollAreaLayer.tryScroll(mouseX, mouseY, scrollAmount);
		scrollAreaType.tryScroll(mouseX, mouseY, scrollAmount);
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.tryScroll(mouseX, mouseY, scrollAmount);

		return false;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scroll) {
		if (scroll != 0) {
			int amount = (int) (scroll / -120f);
			scrollAreaLayer.tryScroll(x, y, amount);
			scrollAreaType.tryScroll(x, y, amount);
			if (scrollAreaAdditionalData != null)
				scrollAreaAdditionalData.tryScroll(x, y, amount);

			return super.mouseScrolled(x, y, scroll);
		}
		return false;
	}

}
