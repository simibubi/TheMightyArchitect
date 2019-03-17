package com.simibubi.mightyarchitect.buildomatico.helpful;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum AllShaders {

	Blueprint("blueprint.json");

	private ResourceLocation location;

	private AllShaders(String filename) {
		location = new ResourceLocation(TheMightyArchitect.ID, "shaders/post/" + filename);
	}

	public boolean isActive() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.entityRenderer.isShaderActive()
				&& mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(location.toString());
	}
	
	public void setActive(boolean active) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (active && !isActive()) 
			mc.entityRenderer.loadShader(location);
		
		if (!active && isActive()) 
			mc.entityRenderer.stopUseShader();
	}

}
