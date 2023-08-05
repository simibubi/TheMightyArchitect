package com.simibubi.mightyarchitect.gui;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TextInputPromptScreen extends AbstractSimiScreen {

	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private EditBox nameField;
	private Button confirm;
	private Button abort;

	private Component buttonTextConfirm;
	private Component buttonTextAbort;
	private Component title;

	private boolean confirmed;

	public TextInputPromptScreen(Consumer<String> callBack, Consumer<String> abortCallback) {
		super();
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = Lang.text("Confirm")
			.component();
		buttonTextAbort = Lang.text("Abort")
			.component();
		confirmed = false;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(ScreenResources.TEXT_INPUT.width, ScreenResources.TEXT_INPUT.height + 30);

		this.nameField = new EditBox(font, topLeftX + 33, topLeftY + 26, 128, 8, Component.empty());
		this.nameField.setTextColor(-1);
		this.nameField.setTextColorUneditable(-1);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(35);
		this.nameField.setFocused(true);
		setFocused(nameField);
		addRenderableWidget(nameField);

		confirm = Button.builder(buttonTextConfirm, button -> {
			callback.accept(nameField.getValue());
			confirmed = true;
			minecraft.setScreen(null);
		})
			.bounds(topLeftX - 5, topLeftY + 50, 100, 20)
			.build();

		abort = Button.builder(buttonTextAbort, button -> {
			minecraft.setScreen(null);
		})
			.bounds(topLeftX + 100, topLeftY + 50, 100, 20)
			.build();

		widgets.add(confirm);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	protected void renderWindow(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
		ScreenResources.TEXT_INPUT.draw(pGuiGraphics, topLeftX, topLeftY);
		pGuiGraphics.drawString(font, title, topLeftX + (sWidth / 2) - (font.width(title) / 2), topLeftY + 11,
			ScreenResources.FONT_COLOR, false);
	}

	@Override
	public void removed() {
		if (!confirmed)
			abortCallback.accept(nameField.getValue());
		super.removed();
	}

	public void setButtonTextConfirm(String buttonTextConfirm) {
		this.buttonTextConfirm = Lang.text(buttonTextConfirm)
			.component();
	}

	public void setButtonTextAbort(String buttonTextAbort) {
		this.buttonTextAbort = Lang.text(buttonTextAbort)
			.component();
	}

	public void setTitle(String title) {
		this.title = Lang.text(title)
			.component();
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			confirm.onPress();
			return true;
		}
		if (keyCode == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		}
		return nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
	}

}
