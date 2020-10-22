package com.simibubi.mightyarchitect.gui;

import java.nio.file.Paths;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.foundation.utility.FilesHelper;
import com.simibubi.mightyarchitect.gui.widgets.IconButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PalettePickerScreen extends AbstractSimiScreen {

	private PaletteButton primary, secondary;
	private IconButton buttonAddPalette;
	private IconButton buttonOpenFolder;
	private IconButton buttonRefresh;
	private boolean scanPicker;

	public PalettePickerScreen() {
		this(false);
	}

	public PalettePickerScreen(boolean scanPicker) {
		super();
		client = Minecraft.getInstance();
		this.scanPicker = scanPicker;

	}

	@Override
	public void init() {
		super.init();
		setWindowSize(256, 236);
		widgets.clear();

		// selected
		updateSelected();

		// resource palettes
		int id = 2;
		int x = topLeftX + 10;
		int y = topLeftY + 68;
		for (String paletteName : PaletteStorage.getResourcePaletteNames()) {
			widgets.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id, x + ((id - 2) % 5) * 23,
				y + ((id - 2) / 5) * 23));
			id++;
		}

		// my palettes
		int i = 0;
		x = topLeftX + 135;
		y = topLeftY + 68;
		for (String paletteName : PaletteStorage.getPaletteNames()) {
			widgets.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id + i, x + (i % 5) * 23,
				y + (i / 5) * 23));
			i++;
		}

		// create
		if (!scanPicker) {
			buttonAddPalette = new IconButton(x + (i % 5) * 23, y + (i / 5) * 23, ScreenResources.ICON_ADD);
			buttonAddPalette.setToolTip(new StringTextComponent("Create Palette"));
			buttonAddPalette.getToolTip()
				.add(new StringTextComponent("Will use currently selected").formatted(TextFormatting.GRAY));
			buttonAddPalette.getToolTip()
				.add(new StringTextComponent("Palette as the template.").formatted(TextFormatting.GRAY));
			i++;
			widgets.add(buttonAddPalette);
		}

		buttonOpenFolder = new IconButton(x + (i % 5) * 23, y + (i / 5) * 23, ScreenResources.ICON_FOLDER);
		buttonOpenFolder.setToolTip(new StringTextComponent("Open Palette Folder"));
		widgets.add(buttonOpenFolder);
		i++;

		buttonRefresh = new IconButton(x + (i % 5) * 23, y + (i / 5) * 23, ScreenResources.ICON_REFRESH);
		buttonRefresh.setToolTip(new StringTextComponent("Refresh Imported Palettes"));
		widgets.add(buttonRefresh);
		i++;

	}

	@Override
	public void removed() {
		super.removed();

		if (scanPicker) {
			if (primary.palette.hasDuplicates())
				client.player.sendStatusMessage(
					new StringTextComponent(TextFormatting.RED + "Warning: Ambiguous Scanner Palette "
						+ TextFormatting.WHITE + "( " + primary.palette.getDuplicates() + " )"),
					false);

			client.player.sendStatusMessage(new StringTextComponent("Updated Default Palette"), true);
			DesignExporter.theme.setDefaultPalette(primary.palette);
			DesignExporter.theme.setDefaultSecondaryPalette(secondary.palette);
		}
	}

	private void updateSelected() {
		if (widgets.contains(primary))
			widgets.remove(primary);
		if (widgets.contains(secondary))
			widgets.remove(secondary);

		if (scanPicker) {
			primary = new PaletteButton(DesignExporter.scanningPalette, this, 0, topLeftX + 135, topLeftY + 8);
			primary.active = false;
			secondary = new PaletteButton(DesignExporter.theme.getDefaultSecondaryPalette(), this, 1, topLeftX + 192,
				topLeftY + 8);
			secondary.active = false;
			widgets.add(primary);
			widgets.add(secondary);
			return;
		}

		primary = new PaletteButton(ArchitectManager.getModel()
			.getPrimary(), this, 0, topLeftX + 135, topLeftY + 8);
		primary.active = false;
		secondary = new PaletteButton(ArchitectManager.getModel()
			.getSecondary(), this, 1, topLeftX + 192, topLeftY + 8);
		secondary.active = false;
		widgets.add(primary);
		widgets.add(secondary);
	}

	@Override
	public void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ScreenResources.PALETTES.draw(ms, this, topLeftX, topLeftY);

		int color = ScreenResources.FONT_COLOR;

		if (scanPicker) {
			textRenderer.draw(ms, "Choose a palette for", topLeftX + 8, topLeftY + 10, color);
			textRenderer.draw(ms, "your theme.", topLeftX + 8, topLeftY + 18, color);

		} else {
			textRenderer.draw(ms, "Palette Picker", topLeftX + 8, topLeftY + 10, color);
			textRenderer.draw(ms, "Primary", topLeftX + 134, topLeftY + 30, color);
			textRenderer.draw(ms, "Secondary", topLeftX + 191, topLeftY + 30, color);

		}

		textRenderer.draw(ms, "Included Palettes", topLeftX + 8, topLeftY + 53, color);
		textRenderer.draw(ms, "My Palettes", topLeftX + 134, topLeftY + 53, color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		for (int i = 0; i < this.widgets.size(); ++i) {
			Widget guibutton = this.widgets.get(i);

			if (guibutton.isMouseOver(mouseX, mouseY)) {
				guibutton.playDownSound(this.client.getSoundHandler());
				if (mouseButton == 0)
					this.buttonClicked(guibutton);
				if (mouseButton == 1)
					this.buttonRightClicked(guibutton);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void buttonClicked(Widget button) {
		if (button == buttonOpenFolder) {
			FilesHelper.createFolderIfMissing("palettes");
			Util.getOSType()
				.openFile(Paths.get("palettes/")
					.toFile());
		}

		if (button == buttonRefresh) {
			PaletteStorage.loadAllPalettes();
			init();
		}

		if (scanPicker) {
			if (button instanceof PaletteButton)
				DesignExporter.scanningPalette = ((PaletteButton) button).palette;
			updateSelected();
			return;
		}

		if (!(button instanceof PaletteButton)) {
			if (button == buttonAddPalette) {
				ArchitectManager.createPalette(true);
				client.displayGuiScreen(null);
			}
		} else {
			ArchitectManager.getModel()
				.swapPrimaryPalette(((PaletteButton) button).palette);
			updateSelected();
			MightyClient.renderer.update();
		}
	}

	protected void buttonRightClicked(Widget button) {
		if (scanPicker) {
			if (button instanceof PaletteButton)
				DesignExporter.theme.setDefaultSecondaryPalette(((PaletteButton) button).palette);
			updateSelected();
			return;
		}

		if (!(button instanceof PaletteButton)) {
			ArchitectManager.createPalette(false);
			client.displayGuiScreen(null);
		} else {
			ArchitectManager.getModel()
				.swapSecondaryPalette(((PaletteButton) button).palette);
			updateSelected();
			MightyClient.renderer.update();
		}
	}

	class PaletteButton extends IconButton {
		Screen parent;
		PaletteDefinition palette;

		public PaletteButton(PaletteDefinition palette, Screen parent, int buttonId, int x, int y) {
			super(x, y, ScreenResources.ICON_NONE);
			this.parent = parent;
			this.palette = palette;
			visible = true;
			active = true;
		}

		private void preview(MatrixStack ms, Minecraft mc) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(x + 5, y + 9, 100);
			RenderSystem.rotatef(-35, 0, 1, 0);
			renderBlock(ms, mc, new BlockPos(0, 1, 0), Palette.INNER_PRIMARY);
			renderBlock(ms, mc, new BlockPos(1, 1, 0), Palette.INNER_DETAIL);
			renderBlock(ms, mc, new BlockPos(0, 0, 0), Palette.HEAVY_PRIMARY);
			renderBlock(ms, mc, new BlockPos(1, 0, 0), Palette.ROOF_PRIMARY);
			RenderSystem.popMatrix();
		}

		protected void renderBlock(MatrixStack ms, Minecraft mc, BlockPos pos, Palette key) {
			RenderSystem.pushMatrix();

			GuiGameElement.of(palette.get(key))
				.atLocal(pos.getX(), pos.getY(), pos.getZ())
				.scale(7.9f)
				.render(ms);
			RenderSystem.popMatrix();
		}

		@Override
		public void renderButton(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(ms, mouseX, mouseY, partialTicks);
			preview(ms, client);
		}

		@Override
		public void renderToolTip(MatrixStack ms, int mouseX, int mouseY) {
			if (hovered) {
				renderTooltip(ms, new StringTextComponent(palette.getName()), mouseX, mouseY);
				RenderSystem.color4f(1, 1, 1, 1);
			}
		}

	}

}
