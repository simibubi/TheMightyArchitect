package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.MightyClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PhasePaused extends PhaseBase {

	@Override
	public void whenEntered() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		player.sendStatusMessage(new StringTextComponent(
			"The Mighty Architect was " + TextFormatting.BOLD + "Paused" + TextFormatting.RESET + "."), false);
		player.sendStatusMessage(new StringTextComponent("You can continue composing with [" + TextFormatting.AQUA
			+ MightyClient.COMPOSE.getBoundKeyLocalizedText()
				.getString()
				.toUpperCase()
			+ TextFormatting.WHITE + "]"), false);
	}

	@Override
	public void update() {

	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {

	}

	@Override
	public void whenExited() {

	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("You have started a build earlier, would you like to continue where you left off?");
	}

}
