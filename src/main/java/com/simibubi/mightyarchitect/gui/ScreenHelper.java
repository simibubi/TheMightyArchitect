package com.simibubi.mightyarchitect.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ScreenHelper {

	private static Screen openedGuiNextTick;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (openedGuiNextTick != null) {
			Minecraft.getInstance().setScreen(openedGuiNextTick);
			openedGuiNextTick = null;
		}
	}
	
	public static void open(Screen gui) {
		openedGuiNextTick = gui;
	}
	
}
