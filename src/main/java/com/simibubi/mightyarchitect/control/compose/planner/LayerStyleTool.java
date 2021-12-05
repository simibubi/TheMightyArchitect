package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.List;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignLayer;

import net.minecraft.ChatFormatting;

public class LayerStyleTool extends WallDecorationToolBase {

	@Override
	public void init() {
		super.init();
		highlightRoom = true;
	}

	@Override
	public boolean handleMouseWheel(int amount) {

		if (selectedRoom == null)
			return false;

		DesignLayer current = selectedRoom.designLayer;
		List<DesignLayer> layers = model.getTheme().getRoomLayers();

		int index = (layers.indexOf(current) + amount + layers.size()) % layers.size();
		DesignLayer newLayer = layers.get(index);
		selectedRoom.designLayer = newLayer;
		model.getTheme().getDesignPicker().rerollRoom(selectedRoom);
		ArchitectManager.reAssemble();
		status("Style: " + ChatFormatting.AQUA + newLayer.getDisplayName());

		return true;
	}

}
