package com.simibubi.mightyarchitect.control.helpful;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum Shaders {

	Blueprint("blueprint.json"), None("");

	private ResourceLocation location;

	private Shaders(String filename) {
		location = new ResourceLocation(TheMightyArchitect.ID, "shaders/post/" + filename);
	}

	public boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		return mc.entityRenderer.isShaderActive()
				&& mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(location.toString());
	}

	public void setActive(boolean active) {
		Minecraft mc = Minecraft.getInstance();

		if (this == None) {
			mc.entityRenderer.stopUseShader();
			return;
		}

		if (active && !isActive()) {
			mc.entityRenderer.loadShader(location);
			return;
		}

		if (!active && isActive()) {
			mc.entityRenderer.stopUseShader();
			return;
		}
	}

}
