package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.Keybinds;
import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PhasePaused extends PhaseBase {

	@Override
	public void whenEntered() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		Lang.text("The Mighty Architect was " + ChatFormatting.BOLD + "Paused" + ChatFormatting.RESET + ".")
			.sendChat(player);
		Lang.text("You can continue composing with [" + ChatFormatting.AQUA + Keybinds.ACTIVATE.getBoundKey()
			+ ChatFormatting.WHITE + "]")
			.sendChat(player);
	}

	@Override
	public void update() {

	}

	@Override
	public void whenExited() {

	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("You have started a build earlier, would you like to continue where you left off?");
	}

}
