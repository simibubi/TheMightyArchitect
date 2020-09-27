package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.design.DesignPicker;
import com.simibubi.mightyarchitect.control.design.DesignPicker.RoomDesignMapping;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class CopyDesignTool extends WallDecorationToolBase {

	protected Design copiedDesign;
	protected DesignType copiedDesignType;
	protected boolean selectingCorners;

	@Override
	public void init() {
		super.init();

		copiedDesign = null;
		toolModeNoCtrl = "Paste";
		toolModeCtrl = "Copy";

		highlightRoom = true;
		highlightRoof = true;
	}

	@Override
	protected void updateSelectedRooms() {
		super.updateSelectedRooms();

		if (selectedRoom != null) {
			boolean zInCorner =
				selectedPos.getZ() == selectedRoom.z || selectedPos.getZ() == selectedRoom.z + selectedRoom.length - 1;
			boolean xInCorner =
				selectedPos.getX() == selectedRoom.x || selectedPos.getX() == selectedRoom.x + selectedRoom.width - 1;
			boolean pasting = !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
			selectingCorners = (xInCorner && zInCorner && !(selectedStack instanceof CylinderStack))
				|| copiedDesignType == DesignType.CORNER && pasting;
			selectingCorners = selectingCorners && !(copiedDesignType == DesignType.WALL && pasting);
		}
	}

	@Override
	public void tickToolOutlines() {
		if (selectedRoom == null) {
			super.tickToolOutlines();
			return;
		}
		if (selectedStack instanceof CylinderStack) {
			super.tickToolOutlines();
			return;
		}
		if (selectedFace.getAxis()
			.isVertical()) {
			super.tickToolOutlines();
			return;
		}

		BlockPos origin = selectedRoom.getOrigin()
			.add(model.getAnchor());

		// Outline corners
		if (selectingCorners) {
			Consumer<BlockPos> renderCorner = pos -> {
				MightyClient.outliner
					.showAABB(pos,
						new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1,
							pos.getY() + selectedRoom.height, pos.getZ() + 1))
					.lineWidth(1 / 8f);
			};

			renderCorner.accept(origin);
			renderCorner.accept(origin.east(selectedRoom.width - 1));
			renderCorner.accept(origin.south(selectedRoom.length - 1));
			renderCorner.accept(origin.east(selectedRoom.width - 1)
				.south(selectedRoom.length - 1));
			return;
		}

		// Outline Walls
		BiConsumer<BlockPos, BlockPos> renderWall = (start, size) -> {
			BlockPos end = start.add(size);
			MightyClient.outliner
				.showAABB(start,
					new AxisAlignedBB(start.getX() - 1 / 2d, start.getY(), start.getZ() - 1 / 2d, end.getX() - 1 / 2d,
						end.getY(), end.getZ() - 1 / 2d))
				.lineWidth(1 / 8f);
		};

		if (selectedFace.getAxis() == Axis.X) {
			renderWall.accept(origin.south(1), selectedRoom.getSize()
				.west(selectedRoom.width - 1)
				.north(1));
			renderWall.accept(origin.east(selectedRoom.width)
				.south(1),
				selectedRoom.getSize()
					.west(selectedRoom.width - 1)
					.north(1));
		} else {
			renderWall.accept(origin.east(1), selectedRoom.getSize()
				.north(selectedRoom.length - 1)
				.west(1));
			renderWall.accept(origin.south(selectedRoom.length)
				.east(1),
				selectedRoom.getSize()
					.north(selectedRoom.length - 1)
					.west(1));

		}

	}

	@Override
	public String handleRightClick() {
		boolean keyDown = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);

		if (copiedDesign == null && !keyDown)
			return TextFormatting.RED + "Ctrl+Click to copy a Design";

		DesignPicker designPicker = model.getTheme()
			.getDesignPicker();

		if (keyDown && selectedRoom != null) {
			if (selectedFace == Direction.UP && selectedRoom == selectedStack.highest()) {
				copiedDesign = designPicker.getCachedRoof(selectedStack);
				copiedDesignType = selectedStack.getRoofType();

			} else if (selectingCorners) {
				copiedDesign = designPicker.getCachedRoom(selectedRoom).corner;
				copiedDesignType = DesignType.CORNER;

			} else if (selectedStack instanceof CylinderStack) {
				copiedDesign = designPicker.getCachedRoom(selectedRoom).wall1;
				copiedDesignType = DesignType.TOWER;

			} else {
				RoomDesignMapping cachedRoom = designPicker.getCachedRoom(selectedRoom);
				copiedDesign = selectedFace.getAxis() == Axis.Z ? cachedRoom.wall1 : cachedRoom.wall2;
				copiedDesignType = DesignType.WALL;
			}

			return "Copied " + TextFormatting.GREEN + copiedDesignType.getDisplayName();
		}

		if (!keyDown && selectedRoom != null) {
			// paste design
			if (DesignType.roofTypes()
				.contains(copiedDesignType)) {
				if (selectedStack.getRoofType() != copiedDesignType)
					return TextFormatting.RED + "Roof types have to match.";
				int facadeWidth = Math.min(selectedStack.highest().width, selectedStack.highest().length);
				if (!copiedDesign.fitsHorizontally(facadeWidth))
					return TextFormatting.RED + "Roof does not fit.";

				designPicker.putRoof(selectedStack, copiedDesign);
				return pasteSuccessful();
			}

			if (copiedDesignType == DesignType.TOWER) {
				if (!(selectedStack instanceof CylinderStack))
					return TextFormatting.RED + "Room shapes have to match.";
				if (!copiedDesign.fitsHorizontally(selectedRoom.width))
					return TextFormatting.RED + "Target needs to have the same diameter.";
				if (!copiedDesign.fitsVertically(selectedRoom.height))
					return TextFormatting.RED + "Design does not fit the targets height.";

				designPicker.putRoom(selectedRoom, new RoomDesignMapping(copiedDesign));
				return pasteSuccessful();
			}

			if (copiedDesignType == DesignType.CORNER) {
				if (selectedStack instanceof CylinderStack)
					return TextFormatting.RED + "Cylinders cannot have corners.";
				if (!copiedDesign.fitsVertically(selectedRoom.height))
					return TextFormatting.RED + "Corner Design cannot fit the required height.";

				RoomDesignMapping priorMapping = designPicker.getCachedRoom(selectedRoom);
				priorMapping.corner = copiedDesign;
				designPicker.putRoom(selectedRoom, priorMapping);

				return pasteSuccessful();
			}

			if (copiedDesignType == DesignType.WALL) {
				if (selectedStack instanceof CylinderStack)
					return TextFormatting.RED + "Room shapes have to match.";
				if (selectedFace.getAxis()
					.isVertical())
					return TextFormatting.RED + "Cannot apply Wall vertically.";

				int wallWidth = selectedFace.getAxis() == Axis.Z ? selectedRoom.width - 2 : selectedRoom.length - 2;
				if (!copiedDesign.fitsHorizontally(wallWidth))
					return TextFormatting.RED + "Wall Design cannot fit the required width.";
				if (!copiedDesign.fitsVertically(selectedRoom.height))
					return TextFormatting.RED + "Wall Design cannot fit the required height.";

				RoomDesignMapping priorMapping = designPicker.getCachedRoom(selectedRoom);
				if (selectedFace.getAxis() == Axis.Z)
					priorMapping.wall1 = copiedDesign;
				else
					priorMapping.wall2 = copiedDesign;
				designPicker.putRoom(selectedRoom, priorMapping);

				return pasteSuccessful();
			}

			return TextFormatting.RED + "Couldn't apply " + copiedDesignType.getDisplayName() + " here.";
		}

		return super.handleRightClick();
	}

	private String pasteSuccessful() {
		ArchitectManager.reAssemble();
		return "Applied " + TextFormatting.GREEN + copiedDesignType.getDisplayName();
	}

}
