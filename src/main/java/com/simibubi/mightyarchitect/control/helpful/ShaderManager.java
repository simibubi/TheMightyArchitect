package com.simibubi.mightyarchitect.control.helpful;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ShaderManager {

	private static Shaders activeShader = Shaders.None;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preRender(RenderTickEvent event) {
		if (Minecraft.getInstance().world == null && activeShader != Shaders.None) {
			stopUsingShaders();
		}
		activeShader.setActive(true);
	}

	public static Shaders getActiveShader() {
		return activeShader;
	}

	public static void setActiveShader(Shaders activeShader) {
		ShaderManager.activeShader = activeShader;
	}
	
	public static void stopUsingShaders() {
		activeShader = Shaders.None;
	}
	
}
