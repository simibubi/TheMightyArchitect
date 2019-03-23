package com.simibubi.mightyarchitect.control.phase.export;

import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;

public class PhaseManageThemes extends PhaseBase {

	@Override
	public void whenEntered() {

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
		return ImmutableList.of("Create your own themes for the architect, or import someone elses.",
				"Drop downloaded theme files into " + Paths.get("themes/").toAbsolutePath().toString());
	}

}
