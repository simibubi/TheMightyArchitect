package com.simibubi.mightyarchitect.control.helpful;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ShaderManager {

	private static Shaders activeShader = Shaders.None;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onClientTick(ClientTickEvent event) {
		if (Minecraft.getInstance().world == null && activeShader != Shaders.None) {
			stopUsingShaders();
		}
	}

	public static Shaders getActiveShader() {
		return activeShader;
	}

	public static void setActiveShader(Shaders activeShader) {
		if (getActiveShader() == activeShader)
			return;
		ShaderManager.activeShader = activeShader;
		activeShader.setActive(true);
	}
	
	public static void stopUsingShaders() {
		activeShader = Shaders.None;
		activeShader.setActive(true);
	}
	
}
