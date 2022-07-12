package com.simibubi.mightyarchitect.control.phase;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.Schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public abstract class PhaseBase implements IArchitectPhase {

	protected Minecraft minecraft;
	
	public PhaseBase() {
		minecraft = Minecraft.getInstance();
	}
	
	@Override
	public void onClick(int button) {
	}

	@Override
	public void onKey(int key, boolean released) {
	}
	
	@Override
	public boolean onScroll(int amount) {
		return false;
	}
	
	protected Schematic getModel() {
		return ArchitectManager.getModel();
	}
	
	protected void sendStatusMessage(String message) {
		if (message == null) 
			return;
		
		minecraft.player.displayClientMessage(new TextComponent(message), true);
	}

	
}
