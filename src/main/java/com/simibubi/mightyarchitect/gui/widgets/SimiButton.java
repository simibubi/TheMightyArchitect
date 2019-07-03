package com.simibubi.mightyarchitect.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;

public class SimiButton extends AbstractButton {	

	private GuiResources icon;
	protected boolean pressed;
	public int id;
	
	public String tooltip;
	
	public SimiButton(int id, int x, int y, GuiResources icon) {
		super(x, y, 16, 16, "");
		this.icon = icon;
		this.id = id;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			ResourceLocation buttonTextures = GuiResources.BUTTON.location;
			ResourceLocation iconTexture = icon.location;
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			GuiResources button = 
					(pressed) ? button = GuiResources.BUTTON_DOWN : 
						(isHovered) ? GuiResources.BUTTON_HOVER : 
							GuiResources.BUTTON;
			
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getInstance().getTextureManager().bindTexture(buttonTextures);
			blit(x, y, button.startX, button.startY, button.width, button.height);
			Minecraft.getInstance().getTextureManager().bindTexture(iconTexture);
			blit(x +1, y +1, icon.startX, icon.startY, icon.width, icon.height);
		}
	}
	
	@Override
	public void onPress() {
		this.pressed = true;
	}
	
	@Override
	public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
		super.onRelease(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}
	
}
