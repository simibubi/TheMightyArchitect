package com.simibubi.mightyarchitect.control.compose.planner;

import org.lwjgl.glfw.GLFW;

import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.gui.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class MoveReshapeTool extends AbstractRoomFaceSelectionTool {

	@Override
	public void init() {
		super.init();
		toolModeNoCtrl = "Move (XZ)";
		toolModeCtrl = "Resize (XZ)";
	}
	
	@Override
	public boolean handleMouseWheel(int scroll) {
		if (selectedRoom != null) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				// Resize
				ThemeStatistics statistics = model.getTheme().getStatistics();
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.offset(selectedFace, scroll);

					if (selectedStack instanceof CylinderStack) {
						if (scroll < 0 && room.width < statistics.MaxTowerRadius * 2 + 1) {
							room.x--;
							room.z--;
							room.width += 2;
							room.length += 2;
						} 
						if (scroll > 0 && room.width > statistics.MinTowerRadius * 2 + 1) {
							room.x++;
							room.z++;
							room.width -= 2;
							room.length -= 2;
						} 
							
						return;
					}
					
					int newWidth = room.width + 2 * diff.getX();
					int newLength = room.length - 2 * diff.getZ();
					
					if (Math.min(newWidth, newLength) < statistics.MinRoomLength)
						return;
					if (Math.max(newWidth, newLength) > statistics.MaxRoomLength)
						return;

					room.x -= diff.getX();
					room.width = newWidth;
					room.z += diff.getZ();
					room.length = newLength;
				});
				selectedStack.highest().roofType = statistics.fallbackRoof(selectedStack.highest(),
						selectedStack instanceof CylinderStack);
				player.sendStatusMessage(new StringTextComponent("Size: " + TextFormatting.AQUA + selectedRoom.width
						+ TextFormatting.WHITE + "x" + TextFormatting.AQUA + selectedRoom.length), true);
			} else {
				// Move
				selectedStack.forRoomAndEachAbove(selectedRoom, room -> {
					BlockPos diff = BlockPos.ZERO.offset(selectedFace, scroll);
					room.move(-diff.getX(), diff.getY(), -diff.getZ());
				});
				player.sendStatusMessage(new StringTextComponent("Position: " + TextFormatting.AQUA + selectedRoom.x
						+ TextFormatting.WHITE + ", " + TextFormatting.AQUA + selectedRoom.z), true);
			}
			return true;
		}

		return super.handleMouseWheel(scroll);
	}

}
