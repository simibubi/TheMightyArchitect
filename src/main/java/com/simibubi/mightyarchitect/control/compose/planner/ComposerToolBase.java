package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;
import com.simibubi.mightyarchitect.foundation.utility.RaycastHelper;
import com.simibubi.mightyarchitect.foundation.utility.RaycastHelper.PredicateTraceResult;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public abstract class ComposerToolBase implements IComposerTool {

	protected String toolModeNoCtrl = null;
	protected String toolModeCtrl = null;
	protected float toolModeYOffset = 0;
	protected float lastToolModeYOffset = 0;
	
	public static Stack selectedStack;
	public static Room selectedRoom;
	public static Direction selectedFace;
	public static BlockPos selectedPos;
	
	protected Schematic model;
	
	@Override
	public void init() {
		model = ArchitectManager.getModel();	
		deselect();
	}

	protected void deselect() {
		selectedStack = null;
		selectedFace = null;
		selectedRoom = null;
		selectedPos = null;
	}
	
	@Override
	public void updateSelection() {
		updateOverlay();
		updateSelectedRooms();
	}
	
	protected void updateSelectedRooms() {
		final GroundPlan groundPlan = ArchitectManager.getModel().getGroundPlan();
		final BlockPos anchor = ArchitectManager.getModel().getAnchor();

		if (groundPlan.isEmpty()) {
			deselect();
			return;
		}

		ClientPlayerEntity player = Minecraft.getInstance().player;

		PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, position -> {
			return groundPlan.getRoomAtPos(position.subtract(anchor)) != null;
		});

		if (result.missed()) {
			deselect();
			return;
		}

		selectedPos = result.getPos().subtract(anchor);
		selectedRoom = groundPlan.getRoomAtPos(selectedPos);
		selectedStack = groundPlan.getStackAtPos(selectedPos);
		selectedFace = result.getFacing();
	}
	
	protected void updateOverlay() {
		lastToolModeYOffset = toolModeYOffset;
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			toolModeYOffset += (12 - toolModeYOffset) * .2f;
		else
			toolModeYOffset *= .8f;
	}
	
	@Override
	public void renderOverlay(MatrixStack ms) {
		RenderSystem.pushMatrix();
		Minecraft mc = Minecraft.getInstance();
		MainWindow mainWindow = mc.getWindow();
		RenderSystem.translated(mainWindow.getScaledWidth() / 2, mainWindow.getScaledHeight() / 2 - 3, 0);
		RenderSystem.translated(25,
				-MathHelper.lerp(mc.getRenderPartialTicks(), lastToolModeYOffset, toolModeYOffset),
				0);

		if (toolModeNoCtrl != null) {
			int color = 0xFFFFFFFF;
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				color = 0x66AACCFF;
			mc.fontRenderer.drawWithShadow(ms, toolModeNoCtrl, 0, 0, color);
		}
		if (toolModeCtrl != null) {
			int color = 0xFFFFFFFF;
			if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				color = 0x66AACCFF;
			mc.fontRenderer.drawWithShadow(ms, toolModeCtrl, 0, 12, color);
		}

		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.popMatrix();
	}
	
	protected void status(String message) {
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(message), true);
	}
	
}
