package com.simibubi.mightyarchitect.buildomatico.helpful;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

@EventBusSubscriber
public class ShaderManager {

	private static AllShaders activeShader = AllShaders.None;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preRender(RenderTickEvent event) {
		activeShader.setActive(true);
	}

	public static AllShaders getActiveShader() {
		return activeShader;
	}

	public static void setActiveShader(AllShaders activeShader) {
		ShaderManager.activeShader = activeShader;
	}
	
	public static void stopUsingShaders() {
		activeShader = AllShaders.None;
	}
	
}
