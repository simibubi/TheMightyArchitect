package com.simibubi.mightyarchitect.buildomatico;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.simibubi.mightyarchitect.buildomatico.model.Schematic;
import com.simibubi.mightyarchitect.buildomatico.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.buildomatico.phase.IArchitectPhase;
import com.simibubi.mightyarchitect.buildomatico.phase.IListenForBlockEvents;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber
public class ArchitectManager {

	private static IArchitectPhase phase = ArchitectPhases.Empty.getPhaseHandler();
	private static Schematic model = new Schematic();
	
	public static void enterPhase(ArchitectPhases newPhase) {
		phase.whenExited();
		phase = newPhase.getPhaseHandler();
		phase.whenEntered();
	}
	
	public static Schematic getModel() {
		return model;
	}
	
	public static boolean inPhase(ArchitectPhases phase) {
		return ArchitectManager.phase == phase.getPhaseHandler();
	}
	
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		phase.update();
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		phase.render();
	}

	@SubscribeEvent
	public static void onRightClick(MouseEvent event) {
		if (event.isButtonstate() && Mouse.isButtonDown(event.getButton())) {
			phase.onClick(event.getButton());
		}
	}
	
	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (!Keyboard.getEventKeyState())
			return;
		
		phase.onKey(Keyboard.getEventKey());
	}
	

	@SubscribeEvent
	public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
		if (phase instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phase).onBlockPlaced(event);
		}
	}
	
	@SubscribeEvent
	public static void onBlockBroken(BlockEvent.BreakEvent event) {
		if (phase instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phase).onBlockBroken(event);
		}		
	}
	
	public static void resetSchematic() {
		model = new Schematic();
	}
	
}
