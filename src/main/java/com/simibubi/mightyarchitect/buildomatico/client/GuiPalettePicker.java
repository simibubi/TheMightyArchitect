package com.simibubi.mightyarchitect.buildomatico.client;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.PaletteStorage;
import com.simibubi.mightyarchitect.gui.GuiResources;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class GuiPalettePicker extends GuiScreen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private PaletteButton primary, secondary;

	public GuiPalettePicker() {
		super();
		mc = Minecraft.getMinecraft();
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
		addButton(new SimiButton(id + i, x + 1 + (i % 5) * 23, y + 1 + (i / 5) * 23, GuiResources.ICON_ADD));
		i++;

	}

	private void updateSelected() {
		if (buttonList.contains(primary))
			buttonList.remove(primary);
		if (buttonList.contains(secondary))
			buttonList.remove(secondary);
		primary = new PaletteButton(PalettePickerClient.getInstance().getPrimary(), this, 0, xTopLeft + 134,
				yTopLeft + 6);
		secondary = new PaletteButton(PalettePickerClient.getInstance().getSecondary(), this, 1, xTopLeft + 191,
				yTopLeft + 6);
		primary.enabled = false;
		secondary.enabled = false;
		buttonList.add(primary);
		buttonList.add(secondary);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GuiResources.PALETTES.draw(this, xTopLeft, yTopLeft);

		super.drawScreen(mouseX, mouseY, partialTicks);

		fontRenderer.drawString("Palette Picker", xTopLeft + 8, yTopLeft + 10, 0x274A44, false);
		fontRenderer.drawString("Included Palettes", xTopLeft + 8, yTopLeft + 53, 0x274A44, false);
		fontRenderer.drawString("My Palettes", xTopLeft + 134, yTopLeft + 53, 0x274A44, false);
		fontRenderer.drawString("Primary", xTopLeft + 134, yTopLeft + 30, 0x274A44, false);
		fontRenderer.drawString("Secondary", xTopLeft + 191, yTopLeft + 30, 0x274A44, false);

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
		if (button instanceof SimiButton) {
			BuildingProcessClient.createPalette(true);
			mc.displayGuiScreen(null);			
		} else {
			PalettePickerClient.getInstance().setPrimary(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
	}

	private void actionRightClickPerformed(GuiButton button) {
		if (button instanceof SimiButton) {
			BuildingProcessClient.createPalette(false);
			mc.displayGuiScreen(null);
		} else {
			PalettePickerClient.getInstance().setSecondary(((PaletteButton) button).palette);
			updateSelected();
			SchematicHologram.getInstance().schematicChanged();
		}
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
			GuiResources.PALETTE_BUTTON.draw(parent, x, y);
			drawPreview(mc);
		}

		private void drawPreview(Minecraft mc) {
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableBlend();
			
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			GlStateManager.translate(x + 4.5f, y + 16.5f, 20);
			GlStateManager.rotate(-22.5f, .3f, 1f, 0f);
			GlStateManager.scale(7, -7, 7);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			mc.getBlockRendererDispatcher().renderBlock(palette.get(Palette.INNER_PRIMARY, EnumFacing.UP), new BlockPos(0,0,0), mc.world, buffer);
			mc.getBlockRendererDispatcher().renderBlock(palette.get(Palette.INNER_DETAIL, EnumFacing.UP), new BlockPos(1,0,0), mc.world, buffer);
			mc.getBlockRendererDispatcher().renderBlock(palette.get(Palette.HEAVY_PRIMARY, EnumFacing.UP), new BlockPos(0,1,0), mc.world, buffer);
			mc.getBlockRendererDispatcher().renderBlock(palette.get(Palette.ROOF_PRIMARY, EnumFacing.UP), new BlockPos(1,1,0), mc.world, buffer);
			
			Tessellator.getInstance().draw();
			GlStateManager.popMatrix();
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
