package com.simibubi.mightyarchitect.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber
public class GuiOpener {

	private static GuiScreen openedGuiNextTick;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (openedGuiNextTick != null) {
			Minecraft.getMinecraft().displayGuiScreen(openedGuiNextTick);
			openedGuiNextTick = null;
		}
	}
	
	public static void open(GuiScreen gui) {
		openedGuiNextTick = gui;
	}
	
}
