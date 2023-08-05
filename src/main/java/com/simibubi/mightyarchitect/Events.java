package com.simibubi.mightyarchitect;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.control.ArchitectManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class Events {

	@Mod.EventBusSubscriber(modid = TheMightyArchitect.ID, bus = MOD, value = Dist.CLIENT)
	public static class ModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {}

		@SubscribeEvent
		public static void register(RegisterKeyMappingsEvent event) {
			Keybinds.register(event);
		}
		
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
			event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "mightyarchitect", ArchitectManager::onDrawGameOverlay);
		}

	}

	@Mod.EventBusSubscriber(modid = TheMightyArchitect.ID, bus = FORGE, value = Dist.CLIENT)
	public static class ForgeEvents {

		@SubscribeEvent
		public static void onTick(ClientTickEvent event) {
			if (event.phase == Phase.START)
				return;
			if (isInLevel())
				MightyClient.tick();
		}

		@SubscribeEvent
		public static void keyPress(InputEvent.Key event) {
			if (isInLevel() && !isInGUI())
				Keybinds.handleKey(event);
		}

		@SubscribeEvent
		public static void mouseButtonPress(InputEvent.MouseButton event) {
			if (isInLevel() && !isInGUI())
				Keybinds.handleMouseButton(event);
		}

		@SubscribeEvent
		public static void onRenderWorld(RenderLevelStageEvent event) {
			if (event.getStage() != Stage.AFTER_PARTICLES)
				return;

			PoseStack ms = event.getPoseStack();
			ms.pushPose();
			BufferSource buffer = Minecraft.getInstance()
				.renderBuffers()
				.bufferSource();

			Vec3 cameraPosition = event.getCamera()
				.getPosition();

			MightyClient.renderer.render(ms, buffer, cameraPosition);
			ArchitectManager.render(ms, buffer, cameraPosition);
			MightyClient.outliner.renderOutlines(ms, buffer, cameraPosition, event.getPartialTick());

			buffer.endLastBatch();
			RenderSystem.enableCull();
			ms.popPose();
		}

		protected static boolean isInLevel() {
			return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
		}

		protected static boolean isInGUI() {
			return Minecraft.getInstance().screen != null;
		}

	}

}