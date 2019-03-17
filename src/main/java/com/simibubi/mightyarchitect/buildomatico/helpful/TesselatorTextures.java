package com.simibubi.mightyarchitect.buildomatico.helpful;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum TesselatorTextures {

	Room("inner.png"),
	SelectedRoom("inner_selected.png"),
	Selection("select.png"),
	Trim("trim.png");
	
	private ResourceLocation location;
	
	private TesselatorTextures(String filename) {
		location = new ResourceLocation(TheMightyArchitect.ID, "textures/blocks/markers/" + filename);
	}
	
	public void bind() {
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
	}
	
}
