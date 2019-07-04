package com.simibubi.mightyarchitect.gui;

import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class GuiTextPrompt extends Screen {

	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private TextFieldWidget nameField;
	private Button confirm;
	private Button abort;

	private int xSize;
	private int ySize;
	private int xTopLeft;
	private int yTopLeft;

	private String buttonTextConfirm;
	private String buttonTextAbort;
	private String title;

	private boolean confirmed;

	public GuiTextPrompt(Consumer<String> callBack, Consumer<String> abortCallback) {
		super(new StringTextComponent("Text Prompt"));
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = "Confirm";
		buttonTextAbort = "Abort";
		confirmed = false;
	}

	@Override
	public void init() {
		super.init();

		xSize = GuiResources.TEXT_INPUT.width;
		ySize = GuiResources.TEXT_INPUT.height + 30;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		this.nameField = new TextFieldWidget(font, xTopLeft + 33, yTopLeft + 26, 128, 8, "");
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.changeFocus(true);

		confirm = new Button(xTopLeft - 5, yTopLeft + 50, 100, 20, buttonTextConfirm, button -> {
			callback.accept(nameField.getText());
			confirmed = true;
			minecraft.displayGuiScreen(null);
		});

		abort = new Button(xTopLeft + 100, yTopLeft + 50, 100, 20, buttonTextAbort, button -> {
			minecraft.displayGuiScreen(null);
		});
		
		buttons.add(confirm);
		buttons.add(abort);

	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();

		GuiResources.TEXT_INPUT.draw(this, xTopLeft, yTopLeft);
		super.render(mouseX, mouseY, partialTicks);

		font.drawString(title, xTopLeft + (xSize / 2) - (font.getStringWidth(title) / 2), yTopLeft + 11,
				GuiResources.FONT_COLOR);
		this.nameField.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public void removed() {
		if (!confirmed)
			abortCallback.accept(nameField.getText());
		super.removed();
	}

	public void setButtonTextConfirm(String buttonTextConfirm) {
		this.buttonTextConfirm = buttonTextConfirm;
	}

	public void setButtonTextAbort(String buttonTextAbort) {
		this.buttonTextAbort = buttonTextAbort;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		
		if (!this.nameField.charTyped(typedChar, keyCode))
			super.charTyped(typedChar, keyCode);
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keyCode == Keyboard.RETURN) {
			confirm.onPress();
			return true;
		}
		this.nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
		return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

}
