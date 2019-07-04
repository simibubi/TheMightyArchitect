package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class GroundPlanningToolBase implements ImAToolForGroundPlanning {

	protected Schematic model;
	protected BlockPos selectedPosition;
	protected Set<Stack> transparentStacks;

	public void init() {
		model = ArchitectManager.getModel();
		selectedPosition = null;
		transparentStacks = new HashSet<>();
	}

	@Override
	public void updateSelection() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		transparentStacks.clear();

		BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.getType() == Type.BLOCK) {

			BlockPos hit = new BlockPos(trace.getHitVec());
			makeStacksTransparent(player, hit);

			boolean replaceable = player.world.getBlockState(hit)
					.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, player.getActiveHand(), trace)));
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
	}

}
