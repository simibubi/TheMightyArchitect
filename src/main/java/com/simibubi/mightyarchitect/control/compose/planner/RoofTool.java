package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.DesignType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class RoofTool extends AbstractRoomFaceSelectionTool {

	@Override
	public boolean handleMouseWheel(int scroll) {
		return super.handleMouseWheel(scroll);
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (selectedStack != null) {
			Minecraft.getInstance().player.sendStatusMessage(
					new StringTextComponent(
							"Roof Type: " + TextFormatting.AQUA + selectedStack.highest().roofType.getDisplayName()),
					true);
		}
	}

	@Override
	public void renderGroundPlan() {
		super.renderGroundPlan();

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

}
