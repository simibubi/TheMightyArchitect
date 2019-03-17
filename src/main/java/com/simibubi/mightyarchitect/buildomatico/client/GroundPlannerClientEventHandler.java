package com.simibubi.mightyarchitect.buildomatico.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.simibubi.mightyarchitect.buildomatico.helpful.AllShaders;

import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class GroundPlannerClientEventHandler {

	private static boolean isActive() {
		return GroundPlannerClient.isActive();
	}

	private static GroundPlannerClient getPlanner() {
		return GroundPlannerClient.getInstance();
	}

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public static void onGuiOpen(GuiOpenEvent event) {
		if (event.getGui() != null && !(event.getGui() instanceof GuiChat) && !(event.getGui() instanceof GuiComposer)
				&& !(event.getGui() instanceof GuiPalettePicker)) {
			if (isActive()) {
				BuildingProcessClient.pauseCompose();
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (isActive()) {
			getPlanner().update();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preRender(RenderTickEvent event) {
		AllShaders.Blueprint.setActive(isActive());
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		if (isActive()) {
			getPlanner().render();
		}
	}

	@SubscribeEvent
	public static void onRightClick(MouseEvent event) {
		if (event.isButtonstate() && event.getButton() == 1 && Mouse.isButtonDown(1)) {
			if (isActive()) {
				getPlanner().handleRightClick();
			}
		}
	}
	
	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (!Keyboard.getEventKeyState())
			return;
		
		if (!GroundPlannerClient.isActive()) 
			return;
		
		switch (Keyboard.getEventKey()) {
		case Keyboard.KEY_UP:
		case Keyboard.KEY_DOWN:
			getPlanner().getActiveTool().getTool().handleKey(Keyboard.getEventKey());
			break;
		case Keyboard.KEY_RIGHT:
			getPlanner().cycleTool(true);
			break;
		case Keyboard.KEY_LEFT:
			getPlanner().cycleTool(false);
			break;
		}
		
	}

}
