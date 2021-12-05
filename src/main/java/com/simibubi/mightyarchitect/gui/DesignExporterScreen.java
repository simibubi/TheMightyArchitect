package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.widgets.Label;
import com.simibubi.mightyarchitect.gui.widgets.ScrollInput;
import com.simibubi.mightyarchitect.gui.widgets.SelectionScrollInput;

public class DesignExporterScreen extends AbstractSimiScreen {

	public DesignExporterScreen() {
		super();
	}

	private ScrollInput scrollAreaLayer;
	private ScrollInput scrollAreaType;
	private ScrollInput scrollAreaAdditionalData;
	private Label labelTheme;
	private Label labelLayer;
	private Label labelType;
	private Label labelAdditionalData;

	private String additionalDataKey;
	private int additionalDataValue;
	private float animationProgress;

	@Override
	public void init() {
		super.init();
		animationProgress = 0;
		setWindowSize(ScreenResources.EXPORTER.width + 100, ScreenResources.EXPORTER.height + 50);

		DesignTheme theme = DesignExporter.theme;
		DesignLayer layer = DesignExporter.layer;
		DesignType type = DesignExporter.type;

		additionalDataValue = DesignExporter.designParameter;

		labelTheme = new Label(topLeftX + 96, topLeftY + 28, "").withShadow();
		labelLayer = new Label(topLeftX + 96, topLeftY + 48, "").withShadow();
		labelType = new Label(topLeftX + 96, topLeftY + 68, "").withShadow();
		labelAdditionalData = new Label(topLeftX + 96, topLeftY + 88, "").withShadow();

		additionalDataKey = "";
		initScrollAreas(theme, layer, type);
	}

	private void initScrollAreas(DesignTheme theme, DesignLayer layer, DesignType type) {
		widgets.clear();

		List<DesignLayer> layers = theme.getLayers();
		labelTheme.setText(theme.getDisplayName());

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

		scrollAreaLayer = new SelectionScrollInput(topLeftX + 93, topLeftY + 45, 90, 14).forOptions(layerOptions)
			.titled("Layer")
			.writingTo(labelLayer)
			.setState(layers.indexOf(layer))
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

		scrollAreaType = new SelectionScrollInput(topLeftX + 93, topLeftY + 65, 90, 14).forOptions(typeOptions)
			.titled("Design Type")
			.writingTo(labelType)
			.setState(types.indexOf(type))
			.calling(position -> {
				DesignExporter.type = types.get(position);
				initAdditionalDataScrollArea(types.get(position));
			});

		widgets.add(scrollAreaType);
		initAdditionalDataScrollArea(type);
	}

	private void initAdditionalDataScrollArea(DesignType type) {
		if (widgets.contains(scrollAreaAdditionalData))
			widgets.remove(scrollAreaAdditionalData);

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
				labelAdditionalData.setText(additionalDataValue + "m");

				if (type == DesignType.ROOF) {
					int min = (type.getMinSize() - 1) / 2;
					int max = (type.getMaxSize() - 1) / 2;

					scrollAreaAdditionalData = new ScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).withRange(min, max)
						.setState((additionalDataValue - 1) / 2)
						.writingTo(labelAdditionalData)
						.calling(position -> {
							additionalDataValue = position * 2 + 1;
							labelAdditionalData.setText(position * 2 + 1 + "m");
						});
					labelAdditionalData.setText(additionalDataValue + "m");

				} else {
					int min = type.getMinSize();
					int max = type.getMaxSize();

					scrollAreaAdditionalData =
						new ScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).withRange(min, max + 1)
							.setState(additionalDataValue)
							.writingTo(labelAdditionalData)
							.calling(position -> {
								additionalDataValue = position;
								labelAdditionalData.setText(position + "m");
							});
				}

			} else if (type.hasSubtypes()) {
				if (additionalDataValue == -1)
					additionalDataValue = 0;

				List<String> subtypeOptions = type.getSubtypeOptions();
				if (additionalDataValue >= subtypeOptions.size())
					additionalDataValue = 0;

				labelAdditionalData.setText(subtypeOptions.get(additionalDataValue));
				scrollAreaAdditionalData =
					new SelectionScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).forOptions(subtypeOptions)
						.writingTo(labelAdditionalData)
						.setState(additionalDataValue)
						.calling(p -> additionalDataValue = p);
			}

			scrollAreaAdditionalData.titled(additionalDataKey);
			widgets.add(scrollAreaAdditionalData);

		} else {

			additionalDataValue = -1;
			additionalDataKey = "";
			labelAdditionalData.setText("");
			scrollAreaAdditionalData = null;

		}
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress++;
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ScreenResources.EXPORTER.draw(ms, this, topLeftX, topLeftY);

		ms.pushPose();
		ms.translate((this.width - this.sWidth) / 2 + 250, 220, 100);
		//RenderSystem.rotatef(-30, .4f, 0, -.2f);
		ms.mulPose(Vector3f.XP.rotationDegrees(-30 * 0.4f));
		ms.mulPose(Vector3f.ZN.rotationDegrees(-30 * 0.2f));
		//RenderSystem.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		ms.mulPose(Vector3f.YP.rotationDegrees(90 + 0.2f * animationProgress));
		ms.scale(100, -100, 100);
		GuiGameElement.of(minecraft.player.getMainHandItem())
			.render(ms);
		ms.popPose();

		int color = ScreenResources.FONT_COLOR;
		font.draw(ms, "Export custom Designs", topLeftX + 10, topLeftY + 10, color);
		font.draw(ms, "Theme", topLeftX + 10, topLeftY + 28, color);
		font.draw(ms, "Building Layer", topLeftX + 10, topLeftY + 48, color);
		font.draw(ms, "Design Type", topLeftX + 10, topLeftY + 68, color);
		font.draw(ms, additionalDataKey, topLeftX + 10, topLeftY + 88, color);
	}

	@Override
	public void removed() {
		DesignTheme theme = DesignExporter.theme;
		DesignExporter.layer = theme.getLayers()
			.get(scrollAreaLayer.getState());

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
