package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiTextPrompt extends GuiScreen {

	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private GuiTextField nameField;
	private GuiButton confirm;
	private GuiButton abort;

	private int xSize;
	private int ySize;
	private int xTopLeft;
	private int yTopLeft;

	private String buttonTextConfirm;
	private String buttonTextAbort;
	private String title;

	private boolean confirmed;

	public GuiTextPrompt(Consumer<String> callBack, Consumer<String> abortCallback) {
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = "Confirm";
		buttonTextAbort = "Abort";
		confirmed = false;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);

		xSize = GuiResources.TEXT_INPUT.width;
		ySize = GuiResources.TEXT_INPUT.height + 30;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		this.nameField = new GuiTextField(0, this.fontRenderer, xTopLeft + 33, yTopLeft + 26, 128, 8);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);

		confirm = new GuiButton(1, xTopLeft - 5, yTopLeft + 50, 100, 20, buttonTextConfirm);
		abort = new GuiButton(2, xTopLeft + 100, yTopLeft + 50, 100, 20, buttonTextAbort);

		buttonList.add(confirm);
		buttonList.add(abort);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		GuiResources.TEXT_INPUT.draw(this, xTopLeft, yTopLeft);

		super.drawScreen(mouseX, mouseY, partialTicks);

		fontRenderer.drawString(title, xTopLeft + (xSize/2) - (fontRenderer.getStringWidth(title)/2), yTopLeft + 11, GuiResources.FONT_COLOR, false);
		
		this.nameField.drawTextBox();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == confirm) {
			callback.accept(nameField.getText());
			confirmed = true;
			mc.displayGuiScreen(null);
		} else if (button == abort) {
			mc.displayGuiScreen(null);
		}
		super.actionPerformed(button);
	}

	@Override
	public void onGuiClosed() {
		if (!confirmed)
			abortCallback.accept(nameField.getText());
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
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

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_RETURN) {
			actionPerformed(confirm);
			return;
		}
		if (!this.nameField.textboxKeyTyped(typedChar, keyCode))
			super.keyTyped(typedChar, keyCode);
	}

}
