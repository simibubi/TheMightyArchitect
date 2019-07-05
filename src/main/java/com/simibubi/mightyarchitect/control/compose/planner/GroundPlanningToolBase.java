package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;
import com.simibubi.mightyarchitect.gui.Keyboard;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class GroundPlanningToolBase implements ImAToolForGroundPlanning {

	protected Schematic model;
	protected BlockPos selectedPosition;
	protected Set<Stack> transparentStacks;

	protected String toolModeNoCtrl = null;
	protected String toolModeCtrl = null;
	protected float toolModeYOffset = 0;
	protected float lastToolModeYOffset = 0;
	
	public void init() {
		model = ArchitectManager.getModel();
		selectedPosition = null;
		transparentStacks = new HashSet<>();
	}

	@Override
	public void updateSelection() {
		updateOverlay();
		
		ClientPlayerEntity player = Minecraft.getInstance().player;
		transparentStacks.clear();

		BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.getType() == Type.BLOCK) {

			BlockPos hit = new BlockPos(trace.getHitVec());
			makeStacksTransparent(player, hit);

			boolean replaceable = player.world.getBlockState(hit)
					.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, trace)));
			if (trace.getFace().getAxis().isVertical() && !replaceable)
				hit = hit.offset(trace.getFace());

			if (model.getAnchor() == null)
				selectedPosition = hit;
			else
				selectedPosition = hit.subtract(model.getAnchor());

		} else {
			selectedPosition = null;
		}

	}

	protected void updateOverlay() {
		lastToolModeYOffset = toolModeYOffset;
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			toolModeYOffset += (12 - toolModeYOffset) * .2f;
		else
			toolModeYOffset *= .8f;
	}

	protected void makeStacksTransparent(ClientPlayerEntity player, BlockPos hit) {
		if (!model.getGroundPlan().isEmpty()) {
			final BlockPos target = hit;
			RaycastHelper.rayTraceUntil(player, 75, pos -> {

				BlockPos localPos = pos.subtract(model.getAnchor());
				model.getGroundPlan().forEachStack(stack -> {
					if (stack.getRoomAtPos(localPos) != null)
						transparentStacks.add(stack);
				});
				return pos.equals(target);

			});
		}
	}

	@Override
	public String handleRightClick() {
		if (selectedPosition == null)
			return null;

		if (model.getAnchor() == null) {
			model.setAnchor(selectedPosition);
			selectedPosition = BlockPos.ZERO;
		}

		return null;
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		return false;
	}

	@Override
	public void renderGroundPlan() {
		GroundPlan groundPlan = model.getGroundPlan();
		BlockPos anchor = model.getAnchor();

		if (groundPlan != null && anchor != null) {
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			TessellatorTextures.Trim.bind();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			groundPlan.forEachStack(stack -> {
				stack.forEach(room -> {

					BlockPos pos = room.getOrigin().add(anchor);
					TessellatorHelper.walls(bufferBuilder, pos, new BlockPos(room.width, 1, room.length), 0.125, false,
							true);

					if (room == stack.highest()) {
						TessellatorHelper.walls(bufferBuilder, pos.add(0, room.height, 0),
								new BlockPos(room.width, 1, room.length), 0.125, false, true);
					}

				});
			});

			Tessellator.getInstance().draw();

			groundPlan.forEachStack(stack -> {
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				if (transparentStacks.contains(stack)) {
					TessellatorTextures.RoomTransparent.bind();
				} else {
					TessellatorTextures.Room.bind();
				}

				stack.forEach(room -> {
					BlockPos pos = room.getOrigin().add(anchor);
					if (room == stack.highest())
						TessellatorHelper.cube(bufferBuilder, pos, room.getSize(), 0, false, false);
					else
						TessellatorHelper.walls(bufferBuilder, pos, room.getSize(), 0, false, false);
				});
				Tessellator.getInstance().draw();
			});

		}
		
		renderRoof();
	}
	
	protected void renderRoof() {
		GroundPlan groundPlan = model.getGroundPlan();
		BlockPos anchor = model.getAnchor();

		if (groundPlan != null && anchor != null) {
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			GlStateManager.color3f(0, 0, 0);
			GlStateManager.lineWidth(10);

			groundPlan.forEachStack(stack -> {
				Room room = stack.highest();
				float x = room.x + anchor.getX();
				float y = room.y + anchor.getY() + .5f;
				float z = room.z + anchor.getZ();
				float h = room.height;
				float l = room.length;
				float w = room.width;

				bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

				DesignType roofType = room.roofType;
				if (stack instanceof CylinderStack && roofType == DesignType.ROOF)
					roofType = DesignType.TOWER_ROOF;
					
				
				switch (roofType) {
				case TOWER_ROOF:
					bufferBuilder.pos(x, y + h, z).endVertex();
					bufferBuilder.pos(x + w / 2, y + h + w, z + l / 2).endVertex();
					bufferBuilder.pos(x, y + h, z + l).endVertex();
					Tessellator.getInstance().draw();
					
					bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
					bufferBuilder.pos(x + w, y + h, z + l).endVertex();
					bufferBuilder.pos(x + w / 2, y + h + w, z + l / 2).endVertex();
					bufferBuilder.pos(x + w, y + h, z).endVertex();
					Tessellator.getInstance().draw();

					bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
				case FLAT_ROOF:
				case TOWER_FLAT_ROOF:
					bufferBuilder.pos(x, y + h, z).endVertex();
					bufferBuilder.pos(x, y + h, z + l).endVertex();
					bufferBuilder.pos(x + w, y + h, z + l).endVertex();
					bufferBuilder.pos(x + w, y + h, z).endVertex();
					bufferBuilder.pos(x, y + h, z).endVertex();
					break;
				case ROOF:
					bufferBuilder.pos(x, y + h, z).endVertex();
					if (w >= l) 
						bufferBuilder.pos(x, y + h + l / 2, z + l / 2).endVertex();
					bufferBuilder.pos(x, y + h, z + l).endVertex();
					if (w < l) 
						bufferBuilder.pos(x + w / 2, y + h + w / 2, z + l).endVertex();
					bufferBuilder.pos(x + w, y + h, z + l).endVertex();
					if (w >= l) 
						bufferBuilder.pos(x + w, y + h + l / 2, z + l / 2).endVertex();
					bufferBuilder.pos(x + w, y + h, z).endVertex();
					if (w < l) 
						bufferBuilder.pos(x + w / 2, y + h + w / 2, z).endVertex();
					bufferBuilder.pos(x, y + h, z).endVertex();
					Tessellator.getInstance().draw();
					bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
					if (w < l) {
						bufferBuilder.pos(x + w / 2, y + h + w / 2, z + l).endVertex();
						bufferBuilder.pos(x + w / 2, y + h + w / 2, z).endVertex();
					} else {
						bufferBuilder.pos(x, y + h + l / 2, z + l / 2).endVertex();
						bufferBuilder.pos(x + w, y + h + l / 2, z + l / 2).endVertex();
					}
					break;
				default:
				case NONE:
					break;
				}
				
				Tessellator.getInstance().draw();
			});

			GlStateManager.color3f(1, 1, 1);
			GlStateManager.lineWidth(1);
		}
	}
	
	@Override
	public void renderOverlay() {
		GlStateManager.pushMatrix();
		MainWindow mainWindow = Minecraft.getInstance().mainWindow;
		GlStateManager.translated(mainWindow.getScaledWidth() / 2, mainWindow.getScaledHeight() / 2 - 3, 0);
		GlStateManager.translated(25,
				-MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), lastToolModeYOffset, toolModeYOffset),
				0);

		if (toolModeNoCtrl != null) {
			int color = 0xFFFFFFFF;
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				color = 0x66AACCFF;
			Minecraft.getInstance().fontRenderer.drawStringWithShadow(toolModeNoCtrl, 0, 0, color);
		}
		if (toolModeCtrl != null) {
			int color = 0xFFFFFFFF;
			if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				color = 0x66AACCFF;
			Minecraft.getInstance().fontRenderer.drawStringWithShadow(toolModeCtrl, 0, 12, color);
		}

		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.popMatrix();
	}
}
