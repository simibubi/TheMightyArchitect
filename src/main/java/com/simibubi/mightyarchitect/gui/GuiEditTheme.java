package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.GuiIndicator;
import com.simibubi.mightyarchitect.gui.widgets.GuiIndicator.State;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class GuiEditTheme extends AbstractSimiScreen {

	private DesignTheme theme;

	private TextFieldWidget inputName;
	private TextFieldWidget inputAuthor;

	private List<GuiIndicator> indicators;
	private List<TextFieldWidget> inputs;

	private SimiButton confirm;

	private int regular, foundation, open, special;
	private int flatRoof, roof;
	private int tower, towerFlatRoof, towerRoof;

	private ScrollArea areaRoomHeight;
	private DynamicLabel labelRoomHeight;

	public GuiEditTheme() {
		super();
		this.theme = DesignExporter.theme;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(GuiResources.THEME_EDITOR.width, GuiResources.THEME_EDITOR.height);

		// init text inputs
		inputs = new ArrayList<>();

		int x = topLeftX + 85;
		int y = topLeftY + 14;
		int id = 0;

		inputName = new TextFieldWidget(font, x, y, 104, 8, "");
		inputName.setText(theme.getDisplayName());
		inputs.add(inputName);

		inputAuthor = new TextFieldWidget(font, x, y + 20, 104, 8, "");
		inputAuthor.setText(theme.getDesigner());
		inputs.add(inputAuthor);

		inputs.forEach(input -> {
			input.setTextColor(-1);
			input.setDisabledTextColour(-1);
			input.setEnableBackgroundDrawing(false);
			input.setMaxStringLength(35);
			input.changeFocus(false);
		});

		// init buttons and indicators
		indicators = new ArrayList<>();

		x = topLeftX + 10;
		y = topLeftY + 75;
		int indexShift = -id;

		regular = id + indexShift;
		SimiButton button = new SimiButton(x, y, GuiResources.ICON_LAYER_REGULAR);
		button.setToolTip("Regular Style [Always enabled]");
		buttons.add(button);
		GuiIndicator guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		foundation = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_LAYER_FOUNDATION);
		button.setToolTip("Foundation Style");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Foundation) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		open = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_LAYER_OPEN);
		button.setToolTip("Open Arcs Style");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Open) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		special = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_LAYER_SPECIAL);
		button.setToolTip("Special Layer");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Special) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x = topLeftX + 10;
		y += 49;

		button = new SimiButton(x, y, GuiResources.ICON_NO_ROOF);
		button.setToolTip("Enable Rooms [Always Enabled]");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		flatRoof = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_FLAT_ROOF);
		button.setToolTip("Flat Roofs");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		roof = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_NORMAL_ROOF);
		button.setToolTip("Gable Roofs");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 40;

		tower = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_TOWER_NO_ROOF);
		button.setToolTip("Enable Towers");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerFlatRoof = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_TOWER_FLAT_ROOF);
		button.setToolTip("Flat Tower Roofs");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER_FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerRoof = id + indexShift;
		button = new SimiButton(x, y, GuiResources.ICON_TOWER_ROOF);
		button.setToolTip("Conical Tower Roofs");
		buttons.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		labelRoomHeight = new DynamicLabel(topLeftX + (theme.getMaxFloorHeight() > 9 ? 102 : 106), topLeftY + 162, "")
				.withShadow();
		labelRoomHeight.text = theme.getMaxFloorHeight() + "m";

		areaRoomHeight = new ScrollArea(topLeftX + 100, topLeftY + 157, 22, 18).withRange(3, 16)
				.titled("Maximum Height").setState(theme.getMaxFloorHeight()).calling(position -> {
					labelRoomHeight.text = position + "m";
					labelRoomHeight.x = position > 9 ? topLeftX + 102 : topLeftX + 106;
				});
		widgets.add(areaRoomHeight);
		widgets.add(labelRoomHeight);

		confirm = new SimiButton(topLeftX + 172, topLeftY + 157, GuiResources.ICON_CONFIRM);
		buttons.add(confirm);
	}

	protected void actionPerformed(SimiButton button) {

		if (button == confirm) {
			minecraft.displayGuiScreen(null);
			return;
		}

		int index = buttons.indexOf(button);

		// not modifiable
		GuiIndicator indicator = indicators.get(index);
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
		GuiIndicator indicator = indicators.get(index);
		indicator.state = State.OFF;

		if (index == tower) {
			deactivate(towerFlatRoof);
			deactivate(towerRoof);
		}
	}

	private void activate(int index) {
		GuiIndicator indicator = indicators.get(index);
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
	public void renderWindow(int mouseX, int mouseY, float partialTicks) {
		GuiResources.THEME_EDITOR.draw(this, topLeftX, topLeftY);

		int x = topLeftX + 10;
		int y = topLeftY + 14;

		font.drawString("Theme name", x, y, GuiResources.FONT_COLOR);
		font.drawString("Designer", x, y + 20, GuiResources.FONT_COLOR);

		y = topLeftY + 75;

		font.drawString("Styles included", x, y - 17, GuiResources.FONT_COLOR);
		font.drawString("Shapes and Roof Types included", x, y + 32, GuiResources.FONT_COLOR);
		font.drawString("Max. Room Height", x, y + 87, GuiResources.FONT_COLOR);
	}

	@Override
	public void removed() {
		super.removed();
		if (!inputName.getText().isEmpty())
			theme.setDisplayName(inputName.getText());
		if (!inputAuthor.getText().isEmpty())
			theme.setDesigner(inputAuthor.getText());

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
		minecraft.player.sendStatusMessage(new StringTextComponent("Theme settings have been updated."), true);
	}

	private boolean roofLayerExists() {
		return activated(roof) || activated(flatRoof) || activated(towerFlatRoof) || activated(towerRoof);
	}

}
