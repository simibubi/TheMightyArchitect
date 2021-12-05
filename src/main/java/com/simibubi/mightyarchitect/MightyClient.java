package com.simibubi.mightyarchitect;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.SchematicRenderer;
import com.simibubi.mightyarchitect.foundation.SuperRenderTypeBuffer;
import com.simibubi.mightyarchitect.foundation.utility.AnimationTickHolder;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;
import com.simibubi.mightyarchitect.foundation.utility.outliner.Outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

@EventBusSubscriber(value = Dist.CLIENT)
public class MightyClient {

	public static KeyMapping COMPOSE;
	public static KeyMapping TOOL_MENU;

	public static SchematicRenderer renderer = new SchematicRenderer();
	public static Outliner outliner = new Outliner();

	public static void init() {
		AllItems.initColorHandlers();
		String modName = TheMightyArchitect.NAME;
		COMPOSE = new KeyMapping("Start composing", Keyboard.G, modName);
		TOOL_MENU = new KeyMapping("Tool Menu (Hold)", Keyboard.LALT, modName);
		ClientRegistry.registerKeyBinding(COMPOSE);
		ClientRegistry.registerKeyBinding(TOOL_MENU);
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;

		AnimationTickHolder.tick();

		if (!isGameActive())
			return;

		ArchitectManager.tickBlockHighlightOutlines();
		MightyClient.outliner.tickOutlines();
		MightyClient.renderer.tick();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		PoseStack ms = event.getMatrixStack();
		Camera info = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 view = info.getPosition();

		ms.pushPose();
		ms.translate(-view.x(), -view.y(), -view.z());
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance()
			.renderBuffers()
			.bufferSource();

		//SuperRenderTypeBuffer b = SuperRenderTypeBuffer.getInstance();

		MightyClient.renderer.render(ms, buffer);
		ArchitectManager.render(ms, buffer);
		MightyClient.outliner.renderOutlines(ms, buffer);

//		ms.push();
//		ms.translate(5, 10, 4);
//		Minecraft.getInstance()
//			.getBlockRendererDispatcher()
//			.renderModel(Blocks.ACACIA_DOOR.getDefaultState(), new BlockPos(0,0,0), Minecraft.getInstance().world,
//				ms, buffer.getBuffer(RenderType.getSolid()), true, new Random(), EmptyModelData.INSTANCE);
//		ms.pop();

		//b.draw();
		buffer.endBatch();
		ms.popPose();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

}
