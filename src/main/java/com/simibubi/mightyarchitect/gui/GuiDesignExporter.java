package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.OptionScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;

import net.minecraft.client.renderer.RenderHelper;

public class GuiDesignExporter extends AbstractSimiScreen {

	public GuiDesignExporter() {
		super();
	}

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
		setWindowSize(GuiResources.EXPORTER.width + 100, GuiResources.EXPORTER.height + 50);

		DesignTheme theme = DesignExporter.theme;
		DesignLayer layer = DesignExporter.layer;
		DesignType type = DesignExporter.type;

		additionalDataValue = DesignExporter.designParameter;

		labelTheme = new DynamicLabel(topLeftX + 96, topLeftY + 28, "").withShadow();
		labelLayer = new DynamicLabel(topLeftX + 96, topLeftY + 48, "").withShadow();
		labelType = new DynamicLabel(topLeftX + 96, topLeftY + 68, "").withShadow();
		labelAdditionalData = new DynamicLabel(topLeftX + 96, topLeftY + 88, "").withShadow();

		additionalDataKey = "";
		initScrollAreas(theme, layer, type);
	}

	private void initScrollAreas(DesignTheme theme, DesignLayer layer, DesignType type) {
		widgets.clear();

		List<DesignLayer> layers = theme.getLayers();
		labelTheme.text = theme.getDisplayName();

		if (!layers.contains(layer))
			layer = DesignLayer.Regular;

		List<String> layerOptions = new ArrayList<>();
		layers.forEach(l -> layerOptions.add(l.getDisplayName()));

//		scrollAreaLayer = new ScrollArea(layerOptions, new IScrollAction() {
//			@Override
//			public void onScroll(int position) {
//				labelLayer.text = layerOptions.get(position);
//				initTypeScrollArea(theme, layers.get(position), DesignExporter.type);
//			}
//		});
//		scrollAreaLayer.setBounds(topLeftX + 93, topLeftY + 45, 90, 14);
//		scrollAreaLayer.setTitle("Style Layer");
//		scrollAreaLayer.setState(layers.indexOf(layer));
//		labelLayer.text = layer.getDisplayName();
//		scrollAreas.add(scrollAreaLayer);

		scrollAreaLayer = new OptionScrollArea(topLeftX + 93, topLeftY + 45, 90, 14).forOptions(layerOptions)
				.titled("Layer").writingTo(labelLayer).setState(layers.indexOf(layer))
				.calling(position -> initTypeScrollArea(theme, layers.get(position), DesignExporter.type));

		widgets.add(labelTheme);
		widgets.add(labelLayer);
		widgets.add(labelType);
		widgets.add(labelAdditionalData);
		widgets.add(scrollAreaLayer);

		initTypeScrollArea(theme, layer, type);
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

		if (widgets.contains(scrollAreaType))
			widgets.remove(scrollAreaType);

//		scrollAreaType = new ScrollArea(typeOptions, new IScrollAction() {
//			@Override
//			public void onScroll(int position) {
//				labelType.text = typeOptions.get(position);
//				DesignExporter.type = types.get(position);
//				initAdditionalDataScrollArea(types.get(position));
//			}
//		});
//		scrollAreaType.setBounds(topLeftX + 93, topLeftY + 65, 90, 14);
//		scrollAreaType.setTitle("Design Type");
//		scrollAreaType.setState(types.indexOf(type));
//		labelType.text = type.getDisplayName();

		scrollAreaType = new OptionScrollArea(topLeftX + 93, topLeftY + 65, 90, 14).forOptions(typeOptions)
				.titled("Design Type").writingTo(labelType).setState(types.indexOf(type)).calling(position -> {
					DesignExporter.type = types.get(position);
					initAdditionalDataScrollArea(types.get(position));
				});

		widgets.add(scrollAreaType);
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

					scrollAreaAdditionalData = new ScrollArea(topLeftX + 93, topLeftY + 85, 90, 14).withRange(min, max)
							.setState((additionalDataValue - 1) / 2).calling(position -> {
								additionalDataValue = position * 2 + 1;
							});

				} else {
					int min = type.getMinSize();
					int max = type.getMaxSize();

					scrollAreaAdditionalData = new ScrollArea(topLeftX + 93, topLeftY + 85, 90, 14)
							.withRange(min, max + 1).setState(additionalDataValue).calling(position -> {
								additionalDataValue = position;
								labelAdditionalData.text = position + "m";
							});
				}

			} else if (type.hasSubtypes()) {
				if (additionalDataValue == -1)
					additionalDataValue = 0;

				List<String> subtypeOptions = type.getSubtypeOptions();
				if (additionalDataValue >= subtypeOptions.size())
					additionalDataValue = 0;

				labelAdditionalData.text = subtypeOptions.get(additionalDataValue);
				scrollAreaAdditionalData = new OptionScrollArea(topLeftX + 93, topLeftY + 85, 90, 14)
						.forOptions(subtypeOptions).setState(additionalDataValue).calling(p -> additionalDataValue = p);
			}

			scrollAreaAdditionalData.titled(additionalDataKey).writingTo(labelAdditionalData);

		} else {

			additionalDataValue = -1;
			additionalDataKey = "";
			labelAdditionalData.text = "";
			scrollAreaAdditionalData = null;

		}
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		GuiResources.EXPORTER.draw(this, topLeftX, topLeftY);

		RenderHelper.enableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translatef((this.width - this.sWidth) / 2 + 250, 280, 100);
		GlStateManager.rotatef(-30, .4f, 0, -.2f);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scalef(300, -300, 300);
		itemRenderer.renderItem(minecraft.player.getHeldItemMainhand(),
				itemRenderer.getModelWithOverrides(minecraft.player.getHeldItemMainhand()));
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		animationProgress++;

		int color = GuiResources.FONT_COLOR;
		font.drawString("Export custom Designs", topLeftX + 10, topLeftY + 10, color);

		font.drawString("Theme", topLeftX + 10, topLeftY + 28, color);
		font.drawString("Building Layer", topLeftX + 10, topLeftY + 48, color);
		font.drawString("Design Type", topLeftX + 10, topLeftY + 68, color);
		font.drawString(additionalDataKey, topLeftX + 10, topLeftY + 88, color);
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

}
