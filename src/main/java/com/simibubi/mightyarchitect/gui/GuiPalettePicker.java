package com.simibubi.mightyarchitect.gui;

import java.io.IOException;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.SchematicHologram;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class GuiPalettePicker extends GuiScreen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private PaletteButton primary, secondary;
	private boolean scanPicker;
	
	public GuiPalettePicker() {
		this(false);
	}
	
	public GuiPalettePicker(boolean scanPicker) {
		super();
		mc = Minecraft.getMinecraft();
		this.scanPicker = scanPicker;
	}

	@Override
	public void initGui() {
		super.initGui();
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
			buttonList.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id, x + ((id - 2) % 5) * 23,
					y + ((id - 2) / 5) * 23));
			id++;
		}

		// my palettes
		int i = 0;
		x = xTopLeft + 134;
		y = yTopLeft + 68;
		for (String paletteName : PaletteStorage.getPaletteNames()) {
			buttonList.add(new PaletteButton(PaletteStorage.getPalette(paletteName), this, id + i, x + (i % 5) * 23,
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
	public void onGuiClosed() {
		super.onGuiClosed();
		
		if (scanPicker) {
			if (primary.palette.hasDuplicates())
				mc.player.sendStatusMessage(new TextComponentString("Warning: Ambiguous Scanner Palette ( " + primary.palette.getDuplicates() + " )"), false);
			
			mc.player.sendStatusMessage(new TextComponentString("Updated Default Palette"), true);
			DesignExporter.theme.setDefaultPalette(primary.palette);
		}
	}
	
	private void updateSelected() {
		if (buttonList.contains(primary))
			buttonList.remove(primary);
		if (buttonList.contains(secondary))
			buttonList.remove(secondary);
		
		if (scanPicker) {
			primary = new PaletteButton(DesignExporter.scanningPalette, this, 0, xTopLeft + 134,
					yTopLeft + 6);
			primary.enabled = false;
			buttonList.add(primary);
			return;
		}
		
		primary = new PaletteButton(ArchitectManager.getModel().getPrimary(), this, 0, xTopLeft + 134,
				yTopLeft + 6);
		primary.enabled = false;
		secondary = new PaletteButton(ArchitectManager.getModel().getSecondary(), this, 1, xTopLeft + 191,
				yTopLeft + 6);
		secondary.enabled = false;
		buttonList.add(primary);
		buttonList.add(secondary);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GuiResources.PALETTES.draw(this, xTopLeft, yTopLeft);

		super.drawScreen(mouseX, mouseY, partialTicks);

		int color = GuiResources.FONT_COLOR;
		
		if (scanPicker) {
			fontRenderer.drawString("Choose a palette for", xTopLeft + 8, yTopLeft + 10, color, false);
			fontRenderer.drawString("scanning your Designs.", xTopLeft + 8, yTopLeft + 18, color, false);
			fontRenderer.drawString("Selected", xTopLeft + 134, yTopLeft + 30, color, false);
			
		} else {
			fontRenderer.drawString("Palette Picker", xTopLeft + 8, yTopLeft + 10, color, false);
			fontRenderer.drawString("Primary", xTopLeft + 134, yTopLeft + 30, color, false);
			fontRenderer.drawString("Secondary", xTopLeft + 191, yTopLeft + 30, color, false);
			
		}
		
		fontRenderer.drawString("Included Palettes", xTopLeft + 8, yTopLeft + 53, color, false);
		fontRenderer.drawString("My Palettes", xTopLeft + 134, yTopLeft + 53, color, false);

		for (GuiButton button : buttonList)
			button.drawButtonForegroundLayer(mouseX, mouseY);

	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 1) {
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton guibutton = this.buttonList.get(i);

				if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
					this.selectedButton = guibutton;
					guibutton.playPressSound(this.mc.getSoundHandler());
					this.actionRightClickPerformed(guibutton);
				}
			}
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (scanPicker) {
			if (button instanceof PaletteButton)
				DesignExporter.scanningPalette = ((PaletteButton) button).palette;
			updateSelected();
			return;
		}
		
		if (button instanceof SimiButton) {
			ArchitectManager.createPalette(true);
			mc.displayGuiScreen(null);			
		} else {
			ArchitectManager.getModel().swapPrimaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}

	protected void actionRightClickPerformed(GuiButton button) {
		if (scanPicker)
			return;
		
		if (button instanceof SimiButton) {
			ArchitectManager.createPalette(false);
			mc.displayGuiScreen(null);
		} else {
			ArchitectManager.getModel().swapSecondaryPalette(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	class PaletteButton extends GuiButton {
		GuiScreen parent;
		PaletteDefinition palette;

		public PaletteButton(PaletteDefinition palette, GuiScreen parent, int buttonId, int x, int y) {
			super(buttonId, x, y, 20, 20, "");
			this.parent = parent;
			this.palette = palette;
			visible = true;
			enabled = true;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//			GuiResources.PALETTE_BUTTON.draw(parent, x, y);
			drawPreview(mc);
		}

		private void drawPreview(Minecraft mc) {
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableBlend();
			
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			GlStateManager.translate(x + 11f, y + 10f, 10);
			GlStateManager.rotate(90f, 1f, 0f, 0f);
			GlStateManager.rotate(90f, 0f, 1f, 0f);
			GlStateManager.rotate(20f, 1f, 0f, 0f);
			GlStateManager.rotate(-10f, 0f, 0f, 1f);
			GlStateManager.scale(8, -8, 8);
			
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
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
				drawHoveringText(palette.getName(), mouseX, mouseY);
				GlStateManager.color(1, 1, 1, 1);
			}
		}

	}

}
