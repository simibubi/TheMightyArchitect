package com.simibubi.mightyarchitect.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
		switch (Guis.getById(ID)) {
		default:
			return null;
				
		}
	}

	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
		switch (Guis.getById(ID)) {
		default:
			return null;
				
		}
	}

}
