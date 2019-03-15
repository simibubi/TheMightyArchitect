package com.simibubi.mightyarchitect.buildomatico.client.tools;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlanRenderer;
import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;
import com.simibubi.mightyarchitect.buildomatico.client.GuiComposer;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Room;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;
import com.simibubi.mightyarchitect.gui.GuiOpener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class RoomTool extends GroundPlanningToolBase {

	private BlockPos firstPosition;

	public RoomTool(GroundPlannerClient planner) {
		super(planner);
	}

	@Override
	public String handleRightClick() {
		super.handleRightClick();

		if (selectedPosition == null)
			return null;

		GroundPlan groundPlan = planner.getGroundPlan();

		if (firstPosition == null) {
			for (Room c : groundPlan.getAll()) {
				if (c.contains(selectedPosition)) {
					GuiOpener.open(new GuiComposer(c));
					return null;
				}
			}

			firstPosition = selectedPosition;
			return "First position marked";

		} else {
			Room room = new Room(firstPosition, selectedPosition.subtract(firstPosition));
			room.width++;
			room.length++;
			room.height = 4;
			int facadeWidth = Math.min(room.width, room.length);

			if (facadeWidth % 2 == 0) {
				return "§cFacade cannot have even width: " + facadeWidth;
			}
			if (facadeWidth < 5) {
				return "§cFacade is too narrow (<5): " + facadeWidth;
			}
			if (facadeWidth > 25) {
				return "§cFacade is too wide (>25): " + facadeWidth;
			}

			room.roofType = facadeWidth > 15 ? DesignType.FLAT_ROOF : DesignType.ROOF;
			groundPlan.add(room, 0);
			firstPosition = null;
			return "§aNew Cuboid has been added";
		}
	}

	@Override
	public void updateSelection(BlockPos selectedPos) {
		super.updateSelection(selectedPos);

		if (firstPosition == null)
			return;

		if (selectedPosition == null)
			return;

		BlockPos size = selectedPosition.subtract(firstPosition);
		if (size.getX() % 2 != 0) {
			selectedPosition = selectedPosition.east(size.getX() > 0 ? 1 : -1);
		}
		if (size.getZ() % 2 != 0) {
			selectedPosition = selectedPosition.south(size.getZ() > 0 ? 1 : -1);
		}
	}

	@Override
	public void render() {
		if (selectedPosition == null) {
			return;
		}
		
		BlockPos anchor = planner.getAnchor();
		BlockPos selectedPos = (anchor != null)? selectedPosition.add(anchor) : selectedPosition;
		BlockPos firstPos = (firstPosition != null)? firstPosition.add(anchor) : null;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GroundPlanRenderer.trimTexture);
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
