package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignLayer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

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

	@Override
	public void handleKeyInput(int key) {
		if (selectedRoom == null)
			return;

		Optional<KeyMapping> mapping = Arrays.stream(Minecraft.getInstance().options.keyHotbarSlots).filter(keyMapping -> keyMapping.getKey().getValue() == key).findFirst();
		if (mapping.isEmpty())
			return;

		DesignLayer currentLayer = selectedRoom.designLayer;
		List<DesignLayer> layers = model.getTheme().getRoomLayers();

		int index = ArrayUtils.indexOf(Minecraft.getInstance().options.keyHotbarSlots, mapping.get());
		if (index > layers.size())
			return;

		DesignLayer newLayer = layers.get(index);

		if (newLayer == currentLayer)
			return;

		selectedRoom.designLayer = newLayer;
		model.getTheme().getDesignPicker().rerollRoom(selectedRoom);
		ArchitectManager.reAssemble();
		status("Style: " + ChatFormatting.AQUA + newLayer.getDisplayName());

	}
}
