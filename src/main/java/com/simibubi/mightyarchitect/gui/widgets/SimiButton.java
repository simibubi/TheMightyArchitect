package com.simibubi.mightyarchitect.gui.widgets;

import com.simibubi.mightyarchitect.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class SimiButton extends GuiButton {	

	private GuiResources icon;
	protected boolean pressed;
	
	public String tooltip;
	
	public SimiButton(int id, int x, int y, GuiResources icon) {
		super(id, x, y, "");
		this.icon = icon;
		this.width = 16;
		this.height = 16;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			ResourceLocation buttonTextures = GuiResources.BUTTON.location;
			ResourceLocation iconTexture = icon.location;
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			this.mouseDragged(mc, mouseX, mouseY);
			
			GuiResources button = 
					(pressed) ? button = GuiResources.BUTTON_DOWN : 
					(hovered) ? GuiResources.BUTTON_HOVER : 
					GuiResources.BUTTON;

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(buttonTextures);
			drawTexturedModalRect(x, y, button.startX, button.startY, button.width, button.height);
			mc.getTextureManager().bindTexture(iconTexture);
			drawTexturedModalRect(x +1, y +1, icon.startX, icon.startY, icon.width, icon.height);
		}
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.pressed = true;
			return true;
		}
		return false;
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		this.pressed = false;
		super.mouseReleased(mouseX, mouseY);
	}
	
	
}
