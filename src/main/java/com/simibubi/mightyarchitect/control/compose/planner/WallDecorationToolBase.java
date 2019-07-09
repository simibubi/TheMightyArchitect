package com.simibubi.mightyarchitect.control.compose.planner;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class WallDecorationToolBase extends ComposerToolBase {

	protected boolean highlightStack;
	protected boolean highlightRoom;
	protected boolean highlightRoof;

	@Override
	public void init() {
		super.init();
		highlightStack = false;
		highlightRoom = false;
		highlightRoof = false;
	}

	@Override
	public boolean handleMouseWheel(int scroll) {
		return false;
	}

	@Override
	public String handleRightClick() {
		return null;
	}

	@Override
	public void renderGroundPlan() {
	}

	@Override
	public void renderTool() {

		GlStateManager.lineWidth(5);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.disableTexture();

		if (highlightStack && selectedStack != null) {
			BlockPos min = selectedStack.lowest().getOrigin().add(model.getAnchor());
			BlockPos max = selectedStack.highest().getOrigin().add(selectedStack.highest().getSize())
					.add(model.getAnchor());
			WorldRenderer.drawBoundingBox(min.getX() - 1 / 2d, min.getY() + 1 / 4d, min.getZ() - 1 / 2d,
					max.getX() + 1 / 2d, max.getY(), max.getZ() + 1 / 2d, 1, 1, 1, 1);
		}

		if (highlightRoom && selectedRoom != null) {
			BlockPos min = selectedRoom.getOrigin().add(model.getAnchor());
			BlockPos max = selectedRoom.getOrigin().add(selectedRoom.getSize()).add(model.getAnchor());

			if (highlightRoof && selectedRoom == selectedStack.highest() && selectedFace == Direction.UP) {
				min = min.add(0, selectedRoom.height, 0);
				max = max.add(0, selectedRoom.height, 0);
				WorldRenderer.drawBoundingBox(min.getX() - 1 / 2d, min.getY() + 1 / 4d, min.getZ() - 1 / 2d,
						max.getX() + 1 / 2d, max.getY(), max.getZ() + 1 / 2d, 1, 1, 1, 1);

			} else {
				WorldRenderer.drawBoundingBox(min.getX() - 1 / 2d, min.getY() + 1 / 4d, min.getZ() - 1 / 2d,
						max.getX() + 1 / 2d, max.getY(), max.getZ() + 1 / 2d, 1, 1, 1, 1);
			}

		}

		GlStateManager.lineWidth(1);
		GlStateManager.enableTexture();

	}

}
