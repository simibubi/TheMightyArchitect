package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.design.DesignPicker;
import com.simibubi.mightyarchitect.control.design.DesignPicker.RoomDesignMapping;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.gui.Keyboard;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
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

	}

	@Override
	public String handleRightClick() {
		boolean keyDown = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);

		if (copiedDesign == null && !keyDown)
			return TextFormatting.RED + "Ctrl+Click to copy a Design";
		
		DesignPicker designPicker = model.getTheme().getDesignPicker();

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
				copiedDesign = selectedFace.getAxis() == Axis.X ? cachedRoom.wall1 : cachedRoom.wall2;
				copiedDesignType = DesignType.WALL;
			}

			return "Copied " + TextFormatting.GREEN + copiedDesignType.getDisplayName();
		}
		
		if (!keyDown && selectedRoom != null) {
			// paste design
			
			return "Applied " + TextFormatting.GREEN + copiedDesignType.getDisplayName();
		}

		return super.handleRightClick();
	}

}
