package com.simibubi.mightyarchitect.control.phase.export;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public class PhaseListThemesForEditing extends PhaseBase {

	@Override
	public void whenEntered() {
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
		return ImmutableList.of("Edit one of your previously created or imported design packs.");
	}

}
