package com.simibubi.mightyarchitect;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.SchematicRenderer;
import com.simibubi.mightyarchitect.foundation.utility.AnimationTickHolder;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;
import com.simibubi.mightyarchitect.foundation.utility.outliner.Outliner;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class MightyClient {

	public static KeyBinding COMPOSE;
	public static KeyBinding TOOL_MENU;

	public static SchematicRenderer renderer = new SchematicRenderer();
	public static Outliner outliner = new Outliner();

	public static void init() {
		AllItems.initColorHandlers();
		String modName = TheMightyArchitect.NAME;
		COMPOSE = new KeyBinding(I18n.format("key.mightyarchitect.compose"), Keyboard.G, modName);
		TOOL_MENU = new KeyBinding(I18n.format("key.mightyarchitect.tool_menu"), Keyboard.LALT, modName);
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
		MatrixStack ms = event.getMatrixStack();
		ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
		Vector3d view = info.getProjectedView();

		ms.push();
		ms.translate(-view.getX(), -view.getY(), -view.getZ());
		IRenderTypeBuffer.Impl buffer = Minecraft.getInstance()
			.getBufferBuilders()
			.getEntityVertexConsumers();

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

		buffer.draw();
		ms.pop();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().world == null || Minecraft.getInstance().player == null);
	}

}
