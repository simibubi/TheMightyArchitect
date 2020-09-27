package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.simibubi.mightyarchitect.AllSpecialTextures;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.foundation.utility.RaycastHelper;
import com.simibubi.mightyarchitect.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public abstract class GroundPlanningToolBase extends ComposerToolBase {

	protected BlockPos selectedPosition;
	protected Set<Stack> transparentStacks;

	public void init() {
		super.init();
		selectedPosition = null;
		transparentStacks = new HashSet<>();
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		ClientPlayerEntity player = Minecraft.getInstance().player;
		transparentStacks.clear();

		BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.getType() == Type.BLOCK) {

			BlockPos hit = new BlockPos(trace.getHitVec());
			makeStacksTransparent(player, hit);

			boolean replaceable = player.world.getBlockState(hit)
				.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, trace)));
			if (trace.getFace()
				.getAxis()
				.isVertical() && !replaceable)
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
		if (!model.getGroundPlan()
			.isEmpty()) {
			final BlockPos target = hit;
			RaycastHelper.rayTraceUntil(player, 75, pos -> {

				BlockPos localPos = pos.subtract(model.getAnchor());
				model.getGroundPlan()
					.forEachStack(stack -> {
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
	public void tickGroundPlanOutlines() {
		GroundPlan groundPlan = model.getGroundPlan();
		BlockPos anchor = model.getAnchor();

		if (groundPlan == null || anchor == null)
			return;

		groundPlan.forEachStack(stack -> {
			boolean stackTransparent = transparentStacks.contains(stack);
			boolean stackHighlighted = isStackHighlighted(stack);
			stack.forEach(room -> {
				boolean roomHighlighted = isRoomHighlighted(room);
				MightyClient.outliner.chaseAABB(room, room.toAABB()
					.offset(anchor))
					.withFaceTexture(roomHighlighted ? AllSpecialTextures.SuperSelectedRoom
						: stackTransparent ? AllSpecialTextures.SelectedRoom
							: stackHighlighted ? AllSpecialTextures.SelectedRoom : stack.getTextureOf(room))
					.colored(0x111111)
					.coloredFaces(0x555555)
					.lineWidth(1 / 8f)
					.fadesAfter(2)
					.hideBottom(room != stack.lowest() && !roomHighlighted)
					.hideTop((room != stack.highest() || room.roofType == DesignType.NONE) && !roomHighlighted);
			});
		});
		tickRoofOutlines();
	}

	protected boolean isStackHighlighted(Stack stack) {
		return false;
	}

	protected boolean isRoomHighlighted(Room room) {
		return false;
	}

	protected void tickRoofOutlines() {
		GroundPlan groundPlan = model.getGroundPlan();
		BlockPos anchor = model.getAnchor();

		groundPlan.forEachStack(stack -> {
			Room room = stack.highest();
			float x = room.x + anchor.getX();
			float y = room.y + anchor.getY();
			float z = room.z + anchor.getZ();
			float h = room.height;
			float l = room.length;
			float w = room.width;

			DesignType roofType = room.roofType;
			if (stack instanceof CylinderStack && roofType == DesignType.ROOF)
				roofType = DesignType.TOWER_ROOF;

			List<OutlineParams> lines = new ArrayList<>();
			String string = stack.toString();

			boolean alongZ = w >= l;
			switch (roofType) {
			case TOWER_ROOF:
				key(string + "A").vertex(x, y + h, z, lines)
					.vertex(x + w / 2, y + h + w, z + l / 2, lines)
					.vertex(x, y + h, z + l, lines)
					.end();
				key(string + "B").vertex(x + w, y + h, z + l, lines)
					.vertex(x + w / 2, y + h + w, z + l / 2, lines)
					.vertex(x + w, y + h, z, lines)
					.end();
				key(string).vertex(x, y + h, z, lines)
					.vertex(x, y + h, z + l, lines)
					.vertex(x + w, y + h, z + l, lines)
					.vertex(x + w, y + h, z, lines)
					.vertex(x, y + h, z, lines)
					.end();
				break;

			case FLAT_ROOF:
			case TOWER_FLAT_ROOF:
				y += .25;
				key(string).vertex(x, y + h, z, lines)
					.vertex(x, y + h, z + l, lines)
					.vertex(x + w, y + h, z + l, lines)
					.vertex(x + w, y + h, z, lines)
					.vertex(x, y + h, z, lines)
					.end();
				y += .5;
				key(string + "A").vertex(x, y + h, z, lines)
					.vertex(x, y + h, z + l, lines)
					.vertex(x + w, y + h, z + l, lines)
					.vertex(x + w, y + h, z, lines)
					.vertex(x, y + h, z, lines)
					.end();

				break;
			case ROOF:
				boolean q = room.quadFacadeRoof;

				key(string).vertex(x, y + h, z, lines);
				if (alongZ || q)
					vertex(x, y + h + l / 2, z + l / 2, lines);
				vertex(x, y + h, z + l, lines);
				if (!alongZ || q)
					vertex(x + w / 2, y + h + w / 2, z + l, lines);
				vertex(x + w, y + h, z + l, lines);
				if (alongZ || q)
					vertex(x + w, y + h + l / 2, z + l / 2, lines);
				vertex(x + w, y + h, z, lines);
				if (!alongZ || q)
					vertex(x + w / 2, y + h + w / 2, z, lines);
				vertex(x, y + h, z, lines).end();

				if (!alongZ || q) {
					key(string + "A").vertex(x + w / 2, y + h + w / 2, z + l, lines)
						.vertex(x + w / 2, y + h + w / 2, z, lines)
						.end();
				}
				if (alongZ || q) {
					key(string + "B").vertex(x, y + h + l / 2, z + l / 2, lines)
						.vertex(x + w, y + h + l / 2, z + l / 2, lines)
						.end();
				}
				break;
			default:
			case NONE:
				break;
			}

			lines.forEach(op -> op.lineWidth(1 / 4f)
				.colored(0x222222));
		});

	}

	private Vec3d prevVertex;
	private int vertexCounter;
	private String key;

	GroundPlanningToolBase key(String key) {
		this.key = key;
		return this;
	}

	GroundPlanningToolBase vertex(double x, double y, double z, List<OutlineParams> lines) {
		Vec3d previousVec = prevVertex;
		prevVertex = new Vec3d(x, y, z);
		if (previousVec == null)
			return this;
		lines.add(MightyClient.outliner.chaseLine(key + vertexCounter, previousVec, prevVertex));
		vertexCounter++;
		return this;
	}

	void end() {
		key = null;
		prevVertex = null;
		vertexCounter = 0;
	}

}
