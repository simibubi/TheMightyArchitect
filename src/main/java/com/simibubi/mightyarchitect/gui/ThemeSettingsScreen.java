package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.gui.widgets.IconButton;
import com.simibubi.mightyarchitect.gui.widgets.Indicator;
import com.simibubi.mightyarchitect.gui.widgets.Indicator.State;
import com.simibubi.mightyarchitect.gui.widgets.Label;
import com.simibubi.mightyarchitect.gui.widgets.ScrollInput;

import net.minecraft.client.gui.components.EditBox;

public class ThemeSettingsScreen extends AbstractSimiScreen {

	private DesignTheme theme;

	private EditBox inputName;
	private EditBox inputAuthor;

	private List<Indicator> indicators;
	private List<EditBox> inputs;
	private List<IconButton> toggleButtons;

	private IconButton confirm;

	private int regular, foundation, open, special;
	private int flatRoof, roof;
	private int tower, towerFlatRoof, towerRoof;

	private ScrollInput areaRoomHeight;
	private Label labelRoomHeight;

	public ThemeSettingsScreen() {
		super();
		this.theme = DesignExporter.theme;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(ScreenResources.THEME_EDITOR.width, ScreenResources.THEME_EDITOR.height);

		// init text inputs
		toggleButtons = new ArrayList<>();
		inputs = new ArrayList<>();

		int x = topLeftX + 85;
		int y = topLeftY + 14;
		int id = 0;

		inputName = new EditBox(font, x, y, 104, 8, Lang.empty());
		inputName.setValue(theme.getDisplayName());
		inputName.changeFocus(false);
		inputs.add(inputName);

		inputAuthor = new EditBox(font, x, y + 20, 104, 8, Lang.empty());
		inputAuthor.setValue(theme.getDesigner());
		inputAuthor.changeFocus(false);
		inputs.add(inputAuthor);

		inputs.forEach(input -> {
			input.setTextColor(-1);
			input.setTextColorUneditable(-1);
			input.setBordered(false);
			input.setMaxLength(35);
			input.changeFocus(false);
		});

		// init toggleButtons and indicators
		indicators = new ArrayList<>();

		x = topLeftX + 10;
		y = topLeftY + 75;
		int indexShift = -id;

		regular = id++ + indexShift;
		IconButton button = new IconButton(x, y, ScreenResources.ICON_LAYER_REGULAR);
		button.setToolTip("Regular Style [Always enabled]");
		toggleButtons.add(button);
		Indicator guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		foundation = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_LAYER_FOUNDATION);
		button.setToolTip("Foundation Style");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers()
			.contains(DesignLayer.Foundation) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		open = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_LAYER_OPEN);
		button.setToolTip("Open Arcs Style");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers()
			.contains(DesignLayer.Open) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		special = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_LAYER_SPECIAL);
		button.setToolTip("Special Layer");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers()
			.contains(DesignLayer.Special) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x = topLeftX + 10;
		y += 49;

		id++;
		button = new IconButton(x, y, ScreenResources.ICON_NO_ROOF);
		button.setToolTip("Enable Rooms [Always Enabled]");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		flatRoof = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_FLAT_ROOF);
		button.setToolTip("Flat Roofs");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes()
			.contains(DesignType.FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		roof = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_NORMAL_ROOF);
		button.setToolTip("Gable Roofs");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes()
			.contains(DesignType.ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 40;

		tower = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_TOWER_NO_ROOF);
		button.setToolTip("Enable Towers");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes()
			.contains(DesignType.TOWER) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerFlatRoof = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_TOWER_FLAT_ROOF);
		button.setToolTip("Flat Tower Roofs");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes()
			.contains(DesignType.TOWER_FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerRoof = id++ + indexShift;
		button = new IconButton(x, y, ScreenResources.ICON_TOWER_ROOF);
		button.setToolTip("Conical Tower Roofs");
		toggleButtons.add(button);
		guiIndicator = new Indicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes()
			.contains(DesignType.TOWER_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		labelRoomHeight =
			new Label(topLeftX + (theme.getMaxFloorHeight() > 9 ? 102 : 106), topLeftY + 162, "").withShadow();
		labelRoomHeight.setText(theme.getMaxFloorHeight() + "m");

		areaRoomHeight = new ScrollInput(topLeftX + 100, topLeftY + 157, 22, 18).withRange(3, 16)
			.titled("Maximum Height")
			.setState(theme.getMaxFloorHeight())
			.calling(position -> {
				labelRoomHeight.setText(position + "m");
				labelRoomHeight.x = position > 9 ? topLeftX + 102 : topLeftX + 106;
			});
		widgets.add(areaRoomHeight);
		widgets.add(labelRoomHeight);

		confirm = new IconButton(topLeftX + 172, topLeftY + 157, ScreenResources.ICON_CONFIRM);
		toggleButtons.add(confirm);

		widgets.addAll(indicators);
		widgets.addAll(inputs);
		widgets.addAll(toggleButtons);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if (button == 0) {
			for (IconButton button2 : toggleButtons) {
				if (button2.isHoveredOrFocused()) {
					buttonClicked(button2);
					return true;
				}
			}
		}

		return super.mouseClicked(x, y, button);
	}

	protected void buttonClicked(IconButton button) {
		if (button == confirm) {
			minecraft.setScreen(null);
			return;
		}

		int index = toggleButtons.indexOf(button);

		// not modifiable
		Indicator indicator = indicators.get(index);
		if (indicator.state == State.YELLOW)
			return;

		if (indicator.state == State.OFF) {
			activate(index);
			return;
		}

		if (indicator.state == State.ON) {
			deactivate(index);
			return;
		}
	}

	private void deactivate(int index) {
		Indicator indicator = indicators.get(index);
		indicator.state = State.OFF;

		if (index == tower) {
			deactivate(towerFlatRoof);
			deactivate(towerRoof);
		}
	}

	private void activate(int index) {
		Indicator indicator = indicators.get(index);
		indicator.state = State.ON;

		if (!activated(tower)) {
			if (index == towerFlatRoof || index == towerRoof)
				activate(tower);
		}
	}

	private boolean activated(int index) {
		return indicators.get(index).state != State.OFF;
	}

	@Override
	public void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		ScreenResources.THEME_EDITOR.draw(ms, this, topLeftX, topLeftY);

		int x = topLeftX + 10;
		int y = topLeftY + 14;

		font.draw(ms, "Theme name", x, y, ScreenResources.FONT_COLOR);
		font.draw(ms, "Designer", x, y + 20, ScreenResources.FONT_COLOR);

		y = topLeftY + 75;

		font.draw(ms, "Styles included", x, y - 17, ScreenResources.FONT_COLOR);
		font.draw(ms, "Shapes and Roof Types included", x, y + 32, ScreenResources.FONT_COLOR);
		font.draw(ms, "Max. Room Height", x, y + 87, ScreenResources.FONT_COLOR);
	}

	@Override
	public void removed() {
		super.removed();
		if (!inputName.getValue()
			.isEmpty())
			theme.setDisplayName(inputName.getValue());
		if (!inputAuthor.getValue()
			.isEmpty())
			theme.setDesigner(inputAuthor.getValue());

		theme.setMaxFloorHeight(areaRoomHeight.getState());

		List<DesignLayer> layers = new ArrayList<>();
		layers.addAll(DesignLayer.defaults());

		if (activated(regular))
			layers.add(DesignLayer.Regular);
		if (activated(foundation))
			layers.add(DesignLayer.Foundation);
		if (activated(open))
			layers.add(DesignLayer.Open);
		if (activated(special))
			layers.add(DesignLayer.Special);

		if (!roofLayerExists())
			layers.remove(DesignLayer.Roofing);

		theme.setLayers(layers);

		List<DesignType> types = new ArrayList<>();
		types.addAll(DesignType.defaults());

		if (activated(flatRoof))
			types.add(DesignType.FLAT_ROOF);
		if (activated(roof))
			types.add(DesignType.ROOF);
		if (activated(tower))
			types.add(DesignType.TOWER);
		if (activated(towerFlatRoof))
			types.add(DesignType.TOWER_FLAT_ROOF);
		if (activated(towerRoof))
			types.add(DesignType.TOWER_ROOF);

		theme.setTypes(types);

		ThemeStorage.exportTheme(theme);
		ThemeStorage.reloadExternal();
		ArchitectManager.editTheme(theme);
		Lang.text("Theme settings have been updated.")
			.sendStatus(minecraft.player);
	}

	private boolean roofLayerExists() {
		return activated(roof) || activated(flatRoof) || activated(towerFlatRoof) || activated(towerRoof);
	}

}
