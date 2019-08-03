package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PhasePaused extends PhaseBase {

	@Override
	public void whenEntered() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		player.sendStatusMessage(
				new StringTextComponent(
						"The Mighty Architect was " + TextFormatting.BOLD + "Paused" + TextFormatting.RESET + "."),
				false);
		player.sendStatusMessage(
				new StringTextComponent("You can continue composing with [" + TextFormatting.AQUA
						+ TheMightyArchitect.COMPOSE.getLocalizedName().toUpperCase() + TextFormatting.WHITE + "]"),
				false);
	}

	@Override
	public void update() {

	}

	@Override
	public void render() {

	}

	@Override
	public void whenExited() {

	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("You have started a build earlier, would you like to continue where you left off?");
	}

}
