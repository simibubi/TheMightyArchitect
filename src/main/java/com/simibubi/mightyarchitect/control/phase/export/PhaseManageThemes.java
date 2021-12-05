package com.simibubi.mightyarchitect.control.phase.export;

import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;

import net.minecraft.client.renderer.MultiBufferSource;

public class PhaseManageThemes extends PhaseBase {

	@Override
	public void whenEntered() {

	}

	@Override
	public void update() {

	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer) {

	}

	@Override
	public void whenExited() {

	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Create your own themes for the architect, or import someone elses.",
				"Drop downloaded theme files into " + Paths.get("themes/").toAbsolutePath().toString());
	}

}
