package com.simibubi.mightyarchitect.foundation.utility;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;

public enum Shaders {

	Blueprint("blueprint.json"), None("");

	private ResourceLocation location;

	private Shaders(String filename) {
		location = new ResourceLocation(TheMightyArchitect.ID, "shaders/post/" + filename);
	}

	public boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		ShaderGroup shaderGroup = mc.gameRenderer.currentEffect();
		return shaderGroup != null && shaderGroup.getName()
			.equals(location.toString());
	}

	public void setActive(boolean active) {
		Minecraft mc = Minecraft.getInstance();

		if (this == None) {
			mc.gameRenderer.shutdownEffect();
			return;
		}

		if (active && !isActive()) {
			mc.gameRenderer.loadEffect(location);
			return;
		}

		if (!active && isActive()) {
			mc.gameRenderer.shutdownEffect();
			return;
		}
	}

}
