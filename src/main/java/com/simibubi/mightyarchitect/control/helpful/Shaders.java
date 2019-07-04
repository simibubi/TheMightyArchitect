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
		return mc.gameRenderer.isShaderActive()
				&& mc.gameRenderer.getShaderGroup().getShaderGroupName().equals(location.toString());
	}

	public void setActive(boolean active) {
		Minecraft mc = Minecraft.getInstance();

		if (this == None) {
			mc.gameRenderer.stopUseShader();
			return;
		}

		if (active && !isActive()) {
			mc.gameRenderer.loadShader(location);
			return;
		}

		if (!active && isActive()) {
			mc.gameRenderer.stopUseShader();
			return;
		}
	}

}
