package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentString;

public class GuiEditTheme extends GuiScreen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private DesignTheme theme;

	private GuiTextField inputName;
	private GuiTextField inputAuthor;

	private List<GuiIndicator> indicators;
	private List<GuiTextField> inputs;
	
	private SimiButton confirm;
	
	private int regular, foundation, open, special;
	private int flatRoof, roof;
	private int tower, towerFlatRoof, towerRoof;
	
	private ScrollArea areaRoomHeight;
	private DynamicLabel labelRoomHeight;

	public GuiEditTheme() {
		this.theme = DesignExporter.theme;
	}

	@Override
	public void initGui() {
		super.initGui();
		xSize = GuiResources.THEME_EDITOR.width;
		ySize = GuiResources.THEME_EDITOR.height;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		// init text inputs
		inputs = new ArrayList<>();

		int x = xTopLeft + 85;
		int y = yTopLeft + 14;
		int id = 0;

		inputName = new GuiTextField(id++, fontRenderer, x, y, 104, 8);
		inputName.setText(theme.getDisplayName());
		inputs.add(inputName);

		inputAuthor = new GuiTextField(id++, fontRenderer, x, y + 20, 104, 8);
		inputAuthor.setText(theme.getDesigner());
		inputs.add(inputAuthor);

		inputs.forEach(input -> {
			input.setTextColor(-1);
			input.setDisabledTextColour(-1);
			input.setEnableBackgroundDrawing(false);
			input.setMaxStringLength(35);
			input.setFocused(false);
		});

		// init buttons and indicators
		indicators = new ArrayList<>();

		x = xTopLeft + 10;
		y = yTopLeft + 75;
		int indexShift = -id;
		
		regular = id + indexShift;
		SimiButton button = new SimiButton(id++, x, y, GuiResources.ICON_LAYER_REGULAR);
		button.tooltip = "Regular Style [Always enabled]";
		buttonList.add(button);
		GuiIndicator guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		foundation = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_LAYER_FOUNDATION);
		button.tooltip = "Foundation Style";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Foundation) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		open = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_LAYER_OPEN);
		button.tooltip = "Open Arcs Style";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Open) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		special = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_LAYER_SPECIAL);
		button.tooltip = "Special Layer";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getLayers().contains(DesignLayer.Special) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x = xTopLeft + 10;
		y += 49;

		button = new SimiButton(id++, x, y, GuiResources.ICON_NO_ROOF);
		button.tooltip = "Enable Rooms [Always Enabled]";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = State.YELLOW;
		indicators.add(guiIndicator);

		x += 20;
		flatRoof = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_FLAT_ROOF);
		button.tooltip = "Flat Roofs";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		roof = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_NORMAL_ROOF);
		button.tooltip = "Gable Roofs";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 40;

		tower = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_TOWER_NO_ROOF);
		button.tooltip = "Enable Towers";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerFlatRoof = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_TOWER_FLAT_ROOF);
		button.tooltip = "Flat Tower Roofs";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER_FLAT_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);

		x += 20;
		towerRoof = id + indexShift;
		button = new SimiButton(id++, x, y, GuiResources.ICON_TOWER_ROOF);
		button.tooltip = "Conical Tower Roofs";
		buttonList.add(button);
		guiIndicator = new GuiIndicator(x, y - 5, "");
		guiIndicator.state = theme.getTypes().contains(DesignType.TOWER_ROOF) ? State.ON : State.OFF;
		indicators.add(guiIndicator);
		
		labelRoomHeight = new DynamicLabel(xTopLeft + ( theme.getMaxFloorHeight() > 9 ? 102 : 106), yTopLeft + 162);
		labelRoomHeight.text = theme.getMaxFloorHeight() + "m";
		areaRoomHeight = new ScrollArea(3, 16, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelRoomHeight.text = position + "m";
				labelRoomHeight.x = position > 9 ? xTopLeft + 102 : xTopLeft + 106;
			}
		});
		areaRoomHeight.setBounds(xTopLeft + 100, yTopLeft + 157, 22, 18);
		areaRoomHeight.setState(theme.getMaxFloorHeight());
		areaRoomHeight.setTitle("Maximum Height");
		areaRoomHeight.setNumeric(true);	
		
		confirm = new SimiButton(id, xTopLeft + 172, yTopLeft + 157, GuiResources.ICON_CONFIRM);
		buttonList.add(confirm);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		
		if (button == confirm) {
			mc.displayGuiScreen(null);
			return;
		}
		
		int index = buttonList.indexOf(button);
		
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		GuiResources.THEME_EDITOR.draw(this, xTopLeft, yTopLeft);

		int x = xTopLeft + 10;
		int y = yTopLeft + 14;

		fontRenderer.drawString("Theme name", x, y, GuiResources.FONT_COLOR, false);
		fontRenderer.drawString("Designer", x, y + 20, GuiResources.FONT_COLOR, false);

		y = yTopLeft + 75;

		fontRenderer.drawString("Styles included", x, y - 17, GuiResources.FONT_COLOR, false);
		fontRenderer.drawString("Shapes and Roof Types included", x, y + 32, GuiResources.FONT_COLOR, false);
		fontRenderer.drawString("Max. Room Height", x, y + 87, GuiResources.FONT_COLOR, false);

		super.drawScreen(mouseX, mouseY, partialTicks);

		inputs.forEach(input -> input.drawTextBox());
		indicators.forEach(e -> e.render(mc, mouseX, mouseY));
		
		labelRoomHeight.draw(this);
		areaRoomHeight.draw(this, mouseX, mouseY);
		
		buttonList.forEach(button -> {
			if (((SimiButton) button).tooltip != null && button.isMouseOver()) {
				drawHoveringText(((SimiButton) button).tooltip, mouseX, mouseY);
			}
		});
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		inputs.forEach(input -> input.mouseClicked(mouseX, mouseY, mouseButton));
		
		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) ? 5 : 1);
		areaRoomHeight.tryScroll(mouseX, mouseY, scrollAmount);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
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
		mc.player.sendStatusMessage(new TextComponentString("Theme settings have been updated."), true);
	}
	
	private boolean roofLayerExists() {
		return activated(roof) || activated(flatRoof) || activated(towerFlatRoof) || activated(towerRoof);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.inputName.textboxKeyTyped(typedChar, keyCode) & !this.inputAuthor.textboxKeyTyped(typedChar, keyCode))
			super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int amount = (int) (scroll / -120f);
			areaRoomHeight.tryScroll(i, j, amount);
		}
	}

}
