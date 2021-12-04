package com.simibubi.mightyarchitect.gui;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextInputPromptScreen extends AbstractSimiScreen {

	private Consumer<String> callback;
	private Consumer<String> abortCallback;

	private TextFieldWidget nameField;
	private Button confirm;
	private Button abort;

	private ITextComponent buttonTextConfirm;
	private ITextComponent buttonTextAbort;
	private ITextComponent title;

	private boolean confirmed;

	public TextInputPromptScreen(Consumer<String> callBack, Consumer<String> abortCallback) {
		super();
		this.callback = callBack;
		this.abortCallback = abortCallback;

		buttonTextConfirm = new StringTextComponent("Confirm");
		buttonTextAbort = new StringTextComponent("Abort");
		confirmed = false;
	}

	@Override
	public void init() {
		super.init();
		setWindowSize(ScreenResources.TEXT_INPUT.width, ScreenResources.TEXT_INPUT.height + 30);

		this.nameField =
			new TextFieldWidget(font, topLeftX + 33, topLeftY + 26, 128, 8, new StringTextComponent(""));
		this.nameField.setTextColor(-1);
		this.nameField.setTextColorUneditable(-1);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(35);
		this.nameField.changeFocus(true);

		confirm = new Button(topLeftX - 5, topLeftY + 50, 100, 20, buttonTextConfirm, button -> {
			callback.accept(nameField.getValue());
			confirmed = true;
			minecraft.setScreen(null);
		});

		abort = new Button(topLeftX + 100, topLeftY + 50, 100, 20, buttonTextAbort, button -> {
			minecraft.setScreen(null);
		});

		widgets.add(confirm);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	public void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ScreenResources.TEXT_INPUT.draw(ms, this, topLeftX, topLeftY);
		font.draw(ms, title, topLeftX + (sWidth / 2) - (font.width(title) / 2), topLeftY + 11,
			ScreenResources.FONT_COLOR);
	}

	@Override
	public void removed() {
		if (!confirmed)
			abortCallback.accept(nameField.getValue());
		super.removed();
	}

	public void setButtonTextConfirm(String buttonTextConfirm) {
		this.buttonTextConfirm = new StringTextComponent(buttonTextConfirm);
	}

	public void setButtonTextAbort(String buttonTextAbort) {
		this.buttonTextAbort = new StringTextComponent(buttonTextAbort);
	}

	public void setTitle(String title) {
		this.title = new StringTextComponent(title);
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
