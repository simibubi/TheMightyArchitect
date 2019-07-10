package com.simibubi.mightyarchitect.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.SchematicHologram;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class GuiPalettePicker extends AbstractSimiScreen {

	private PaletteButton primary, secondary;
	private SimiButton buttonAddPalette;
	private boolean scanPicker;

	public GuiPalettePicker() {
		this(false);
	}

	public GuiPalettePicker(boolean scanPicker) {
		super();
		minecraft = Minecraft.getInstance();
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
		int x = topLeftX + 7;
		int y = topLeftY + 68;
		for (String paletteName : PaletteStorage.getResourcePaletteNames()) {
			widgets.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id, x + ((id - 2) % 5) * 23,
					y + ((id - 2) / 5) * 23));
			id++;
		}

		// my palettes
		int i = 0;
		x = topLeftX + 134;
		y = topLeftY + 68;
		for (String paletteName : PaletteStorage.getPaletteNames()) {
			widgets.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id + i, x + (i % 5) * 23,
					y + (i / 5) * 23));
			i++;
		}

		// create
		if (!scanPicker) {
			buttonAddPalette = new SimiButton(x + (i % 5) * 23, y + (i / 5) * 23, GuiResources.ICON_ADD);
			buttonAddPalette.setToolTip("Create Palette");
			widgets.add(buttonAddPalette);
			i++;
		}

	}

	@Override
	public void removed() {
		super.removed();

		if (scanPicker) {
			if (primary.palette.hasDuplicates())
				minecraft.player.sendStatusMessage(
						new StringTextComponent(
								"Warning: Ambiguous Scanner Palette ( " + primary.palette.getDuplicates() + " )"),
						false);

			minecraft.player.sendStatusMessage(new StringTextComponent("Updated Default Palette"), true);
			DesignExporter.theme.setDefaultPalette(primary.palette);
		}
	}

	private void updateSelected() {
		if (widgets.contains(primary))
			widgets.remove(primary);
		if (widgets.contains(secondary))
			widgets.remove(secondary);

		if (scanPicker) {
			primary = new PaletteButton(DesignExporter.scanningPalette, this, 0, topLeftX + 134, topLeftY + 6);
			primary.active = false;
			widgets.add(primary);
			return;
		}

		primary = new PaletteButton(ArchitectManager.getModel().getPrimary(), this, 0, topLeftX + 134, topLeftY + 6);
		primary.active = false;
		secondary = new PaletteButton(ArchitectManager.getModel().getSecondary(), this, 1, topLeftX + 191,
				topLeftY + 6);
		secondary.active = false;
		widgets.add(primary);
		widgets.add(secondary);
	}

	@Override
	public void renderWindow(int mouseX, int mouseY, float partialTicks) {
		GuiResources.PALETTES.draw(this, topLeftX, topLeftY);

		int color = GuiResources.FONT_COLOR;

		if (scanPicker) {
			font.drawString("Choose a palette for", topLeftX + 8, topLeftY + 10, color);
			font.drawString("scanning your Designs.", topLeftX + 8, topLeftY + 18, color);
			font.drawString("Selected", topLeftX + 134, topLeftY + 30, color);

		} else {
			font.drawString("Palette Picker", topLeftX + 8, topLeftY + 10, color);
			font.drawString("Primary", topLeftX + 134, topLeftY + 30, color);
			font.drawString("Secondary", topLeftX + 191, topLeftY + 30, color);

		}

		font.drawString("Included Palettes", topLeftX + 8, topLeftY + 53, color);
		font.drawString("My Palettes", topLeftX + 134, topLeftY + 53, color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		for (int i = 0; i < this.widgets.size(); ++i) {
			Widget guibutton = this.widgets.get(i);

			if (guibutton.isMouseOver(mouseX, mouseY)) {
				guibutton.playDownSound(this.minecraft.getSoundHandler());
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
		if (scanPicker) {
			if (button instanceof PaletteButton)
				DesignExporter.scanningPalette = ((PaletteButton) button).palette;
			updateSelected();
			return;
		}

		if (!(button instanceof PaletteButton)) {
			ArchitectManager.createPalette(true);
			minecraft.displayGuiScreen(null);
		} else {
			ArchitectManager.getModel().swapPrimaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}

	protected void buttonRightClicked(Widget button) {
		if (scanPicker)
			return;

		if (!(button instanceof PaletteButton)) {
			ArchitectManager.createPalette(false);
			minecraft.displayGuiScreen(null);
		} else {
			ArchitectManager.getModel().swapSecondaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}

	class PaletteButton extends SimiButton {
		Screen parent;
		PaletteDefinition palette;

		public PaletteButton(PaletteDefinition palette, Screen parent, int buttonId, int x, int y) {
			super(x, y, GuiResources.ICON_NONE);
			this.parent = parent;
			this.palette = palette;
			visible = true;
			active = true;
		}

		private void drawPreview(Minecraft mc) {
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableBlend();

			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			GlStateManager.translatef(x + 1, y + 17, 10);
			GlStateManager.scalef(8, -8, 8);

			renderBlock(mc, buffer, new BlockPos(0, 1, 1), Palette.INNER_PRIMARY);
			renderBlock(mc, buffer, new BlockPos(1, 1, 1), Palette.INNER_DETAIL);
			renderBlock(mc, buffer, new BlockPos(0, 0, 0), Palette.HEAVY_PRIMARY);
			renderBlock(mc, buffer, new BlockPos(1, 0, 0), Palette.ROOF_PRIMARY);

			GlStateManager.popMatrix();
		}

		protected void renderBlock(Minecraft mc, BufferBuilder buffer, BlockPos pos, Palette key) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(pos.getX(), pos.getY(), pos.getZ());
			IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(palette.get(key));
			mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(model, palette.get(key), this.isHovered? 1 : .9f,
					true);
			GlStateManager.popMatrix();
		}

		@Override
		public void renderButton(int mouseX, int mouseY, float partialTicks) {
			super.renderButton(mouseX, mouseY, partialTicks);
			drawPreview(minecraft);
		}

		@Override
		public void renderToolTip(int mouseX, int mouseY) {
			if (isHovered) {
				renderTooltip(palette.getName(), mouseX, mouseY);
				GlStateManager.color4f(1, 1, 1, 1);
			}
		}

	}

}
