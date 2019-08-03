package com.simibubi.mightyarchitect.control.compose.planner;

import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.control.helpful.Keyboard;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class RoomTool extends GroundPlanningToolBase {

	protected BlockPos firstPosition;
	protected Stack lastAddedStack;

	@Override
	public void init() {
		super.init();
		firstPosition = null;
		toolModeNoCtrl = "3-Grid";
		toolModeCtrl = "5-Grid";
		
		// Deleted using stack tool
		if (lastAddedStack != null && lastAddedStack.floors() == 0)
			lastAddedStack = null;
	}

	@Override
	public String handleRightClick() {
		super.handleRightClick();

		if (selectedPosition == null)
			return null;

		if (firstPosition == null) {
			firstPosition = selectedPosition;
			return "First position marked";

		} else {
			return createRoom(ArchitectManager.getModel().getGroundPlan());
		}
	}

	protected String createRoom(GroundPlan groundPlan) {
		Room room = new Room(firstPosition, selectedPosition.subtract(firstPosition));
		room.width++;
		room.length++;

		DesignTheme theme = groundPlan.theme;
		ThemeStatistics stats = theme.getStatistics();
		boolean hasFoundation = theme.getLayers().contains(DesignLayer.Foundation);
		room.designLayer = hasFoundation ? DesignLayer.Foundation : DesignLayer.Regular;

		int facadeWidth = Math.min(room.width, room.length);

		if (facadeWidth % 2 == 0) {
			return "Facade cannot have even width: " + facadeWidth;
		}
		if (facadeWidth < stats.MinRoomLength) {
			return "Facade is too narrow (<" + stats.MinRoomLength + "): " + facadeWidth;
		}
		if (room.width > stats.MaxRoomLength) {
			return "Room is too large (>" + stats.MaxRoomLength + "): " + room.width;
		}
		if (room.length > stats.MaxRoomLength) {
			return "Room is too large (>" + stats.MaxRoomLength + "): " + room.length;
		}

		if (facadeWidth > stats.MaxGableRoof || !stats.hasGables) {
			room.roofType = stats.hasFlatRoof ? DesignType.FLAT_ROOF : DesignType.NONE;
		} else {
			room.roofType = stats.hasGables ? DesignType.ROOF : DesignType.NONE;
		}

		lastAddedStack = new Stack(room);
		if (!adjustHeightForIntersection(groundPlan, room))
			room.height = theme.getDefaultHeightForFloor(0);

		groundPlan.addStack(lastAddedStack);
		firstPosition = null;
		return "New Room has been added";
	}

	protected boolean adjustHeightForIntersection(GroundPlan groundPlan, Room room) {
		final MutableObject<Room> biggestRoom = new MutableObject<>();
		if (room.height == 0)
			room.height = 1;

		groundPlan.forEachRoom(r -> {
			if (r.intersects(room) && !(r.y + r.height <= room.y || room.y + room.height <= r.y)
					&& (biggestRoom.getValue() == null
							|| biggestRoom.getValue().width * biggestRoom.getValue().length < r.width * r.length)) {
				biggestRoom.setValue(r);
			}
		});

		if (biggestRoom.getValue() != null) {
			room.height = (biggestRoom.getValue().y + biggestRoom.getValue().height) - room.y;
			if (room.height > groundPlan.theme.getMaxFloorHeight())
				room.height -= biggestRoom.getValue().height;
			return true;
		}

		return false;
	}

	public static void increaseMatchingOthers(GroundPlan groundPlan, Stack stack) {
		stack.increase();
		Room added = stack.highest();
		final MutableObject<Room> biggestRoom = new MutableObject<>();
		groundPlan.forEachRoom(r -> {
			if (r == added)
				return;
			if (r.intersects(added) && r.y <= added.y && r.y + r.height > added.y && (biggestRoom.getValue() == null
					|| biggestRoom.getValue().width * biggestRoom.getValue().length < r.width * r.length)) {
				biggestRoom.setValue(r);
			}
		});
		if (biggestRoom.getValue() != null) {
			added.height = (biggestRoom.getValue().y + biggestRoom.getValue().height) - added.y;
		}
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		if (lastAddedStack == null)
			return false;

		if (scroll > 0) {
			increaseMatchingOthers(ArchitectManager.getModel().getGroundPlan(), lastAddedStack);
		} else {
			lastAddedStack.decrease();
			if (lastAddedStack.floors() == 0) {
				ArchitectManager.getModel().getGroundPlan().remove(lastAddedStack);
				lastAddedStack = null;
			}
		}

		return true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (firstPosition == null)
			return;

		if (selectedPosition == null)
			return;

		BlockPos size = selectedPosition.subtract(firstPosition);

		int xSize = size.getX();
		int zSize = size.getZ();

		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			// 5-Grid
			int xr = (xSize + ((xSize > 0) ? 2 : -2)) % 4;
			if (xr < 0)
				xr += 4;
			else
				xr = 4 - xr;
			if (xr != 0) {
				selectedPosition = selectedPosition.east(xSize > 0 ? xr : -xr);
			}
			int zr = (zSize + ((zSize > 0) ? 2 : -2)) % 4;
			if (zr < 0)
				zr += 4;
			else
				zr = 4 - zr;
			if (zr != 0) {
				selectedPosition = selectedPosition.south(zSize > 0 ? zr : -zr);
			}

		} else {
			// 3-Grid
			if (xSize % 2 != 0) {
				selectedPosition = selectedPosition.east(xSize > 0 ? 1 : -1);
			}
			if (zSize % 2 != 0) {
				selectedPosition = selectedPosition.south(zSize > 0 ? 1 : -1);
			}
		}
	}

	@Override
	public void renderTool() {
		if (selectedPosition == null) {
			return;
		}

		BlockPos anchor = ArchitectManager.getModel().getAnchor();
		BlockPos selectedPos = (anchor != null) ? selectedPosition.add(anchor) : selectedPosition;
		BlockPos firstPos = (firstPosition != null) ? firstPosition.add(anchor) : null;

		TessellatorTextures.Selection.bind();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		TessellatorHelper.walls(bufferBuilder, selectedPos, new BlockPos(1, 1, 1), 0.125, false, true);

		if (firstPos != null) {
			BlockPos size = selectedPos.subtract(firstPos);
			Cuboid selection = new Cuboid(firstPos, size.getX(), 1, size.getZ());
			selection.width += 1;
			selection.length += 1;
			TessellatorHelper.walls(bufferBuilder, selection.getOrigin(), selection.getSize(), -0.125, false, true);
			Tessellator.getInstance().draw();

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z - 1, true, false);

			TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z + selection.length + 1, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x + selection.width + 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

			TessellatorHelper.drawString("" + selection.length, selection.x - 1, selection.y + .5f,
					selection.z + selection.length / 2f, true, false);

		} else {
			Tessellator.getInstance().draw();
		}

	}

}
