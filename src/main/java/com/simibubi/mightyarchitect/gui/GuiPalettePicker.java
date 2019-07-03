package com.simibubi.mightyarchitect.gui;

import java.io.IOException;

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
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class GuiPalettePicker extends Screen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private PaletteButton primary, secondary;
	private boolean scanPicker;
	
	public GuiPalettePicker() {
		this(false);
	}
	
	public GuiPalettePicker(boolean scanPicker) {
		super(new StringTextComponent("Palette Picker"));
		minecraft = Minecraft.getInstance();
		this.scanPicker = scanPicker;
	}

	@Override
	public void init() {
		super.init();
		xSize = 256;
		ySize = 236;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		// selected
		updateSelected();

		// resource palettes
		int id = 2;
		int x = xTopLeft + 7;
		int y = yTopLeft + 68;
		for (String paletteName : PaletteStorage.getResourcePaletteNames()) {
			buttons.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id, x + ((id - 2) % 5) * 23,
					y + ((id - 2) / 5) * 23));
			id++;
		}

		// my palettes
		int i = 0;
		x = xTopLeft + 134;
		y = yTopLeft + 68;
		for (String paletteName : PaletteStorage.getPaletteNames()) {
			buttons.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id + i, x + (i % 5) * 23,
					y + (i / 5) * 23));
			i++;
		}

		// create
		if (!scanPicker) {
			addButton(new SimiButton(id + i, x + 1 + (i % 5) * 23, y + 1 + (i / 5) * 23, GuiResources.ICON_ADD));
			i++;			
		}

	}

	@Override
	public void onClose() {
		super.onClose();
		
		if (scanPicker) {
			if (primary.palette.hasDuplicates())
				minecraft.player.sendStatusMessage(new StringTextComponent("Warning: Ambiguous Scanner Palette ( " + primary.palette.getDuplicates() + " )"), false);
			
			minecraft.player.sendStatusMessage(new StringTextComponent("Updated Default Palette"), true);
			DesignExporter.theme.setDefaultPalette(primary.palette);
		}
	}
	
	private void updateSelected() {
		if (buttons.contains(primary))
			buttons.remove(primary);
		if (buttons.contains(secondary))
			buttons.remove(secondary);
		
		if (scanPicker) {
			primary = new PaletteButton(DesignExporter.scanningPalette, this, 0, xTopLeft + 134,
					yTopLeft + 6);
			primary.active = false;
			buttons.add(primary);
			return;
		}
		
		primary = new PaletteButton(ArchitectManager.getModel().getPrimary(), this, 0, xTopLeft + 134,
				yTopLeft + 6);
		primary.active = false;
		secondary = new PaletteButton(ArchitectManager.getModel().getSecondary(), this, 1, xTopLeft + 191,
				yTopLeft + 6);
		secondary.active = false;
		buttons.add(primary);
		buttons.add(secondary);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		GuiResources.PALETTES.draw(this, xTopLeft, yTopLeft);

		super.render(mouseX, mouseY, partialTicks);

		int color = GuiResources.FONT_COLOR;
		
		if (scanPicker) {
			font.drawString("Choose a palette for", xTopLeft + 8, yTopLeft + 10, color);
			font.drawString("scanning your Designs.", xTopLeft + 8, yTopLeft + 18, color);
			font.drawString("Selected", xTopLeft + 134, yTopLeft + 30, color);
			
		} else {
			font.drawString("Palette Picker", xTopLeft + 8, yTopLeft + 10, color);
			font.drawString("Primary", xTopLeft + 134, yTopLeft + 30, color);
			font.drawString("Secondary", xTopLeft + 191, yTopLeft + 30, color);
			
		}
		
		font.drawString("Included Palettes", xTopLeft + 8, yTopLeft + 53, color);
		font.drawString("My Palettes", xTopLeft + 134, yTopLeft + 53, color);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 1) {
			for (int i = 0; i < this.buttons.size(); ++i) {
				Widget guibutton = this.buttons.get(i);

				if (guibutton.keyPressed(mouseX, mouseY, mouseButton)) {
					guibutton.playDownSound(this.minecraft.getSoundHandler());
					this.actionRightClickPerformed((AbstractButton) guibutton);
				}
			}
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	protected void actionPerformed(AbstractButton button) {
		if (scanPicker) {
			if (button instanceof PaletteButton)
				DesignExporter.scanningPalette = ((PaletteButton) button).palette;
			updateSelected();
			return;
		}
		
		if (button instanceof SimiButton) {
			ArchitectManager.createPalette(true);
			minecraft.displayGuiScreen(null);			
		} else {
			ArchitectManager.getModel().swapPrimaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}

	protected void actionRightClickPerformed(AbstractButton button) {
		if (scanPicker)
			return;
		
		if (button instanceof SimiButton) {
			ArchitectManager.createPalette(false);
			minecraft.displayGuiScreen(null);
		} else {
			ArchitectManager.getModel().swapSecondaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}

	class PaletteButton extends AbstractButton {
		Screen parent;
		PaletteDefinition palette;

		public PaletteButton(PaletteDefinition palette, Screen parent, int buttonId, int x, int y) {
			super(x, y, 20, 20, "");
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
			GlStateManager.translatef(x + 11f, y + 10f, 10);
			GlStateManager.rotatef(90f, 1f, 0f, 0f);
			GlStateManager.rotatef(90f, 0f, 1f, 0f);
			GlStateManager.rotatef(20f, 1f, 0f, 0f);
			GlStateManager.rotatef(-10f, 0f, 0f, 1f);
			GlStateManager.scalef(8, -8, 8);
			
			renderBlock(mc, buffer, new BlockPos(0,0,0), Palette.INNER_PRIMARY);
			renderBlock(mc, buffer, new BlockPos(1,0,0), Palette.INNER_DETAIL);
			renderBlock(mc, buffer, new BlockPos(0,1,0), Palette.HEAVY_PRIMARY);
			renderBlock(mc, buffer, new BlockPos(1,1,0), Palette.ROOF_PRIMARY);
			
			GlStateManager.popMatrix();
		}

		protected void renderBlock(Minecraft mc, BufferBuilder buffer, BlockPos pos, Palette key) {
			IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(palette.get(key));
//			mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(mc.world, model, palette.get(key), pos, buffer, false);
			mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(model, palette.get(key), 1, true);
//			
		}

		@Override
		public void renderButton(int mouseX, int mouseY, float p_renderButton_3_) {
			drawPreview(minecraft);
			
			super.renderButton(mouseX, mouseY, p_renderButton_3_);
			
			if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
				renderTooltip(palette.getName(), mouseX, mouseY);
				GlStateManager.color4f(1, 1, 1, 1);
			}
		}
		
		@Override
		public void onPress() {
			
		}

	}

}
