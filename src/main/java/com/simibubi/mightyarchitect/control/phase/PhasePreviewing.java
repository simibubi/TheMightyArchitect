package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.SchematicHologram;

public class PhasePreviewing extends PhaseBase {

	@Override
	public void whenEntered() {
		SchematicHologram.display(getModel());
	}

	@Override
	public void update() {
	}

	@Override
	public void onClick(int button) {
	}

	@Override
	public void onKey(int key) {
	}

	@Override
	public void render() {
	}

	@Override
	public void whenExited() {
		SchematicHologram.reset();
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Here is a preview of your new build.", "From here you can pick your materials in the palette picker [C]", "Once you are happy with what you see, save or build your structure.");
	}
	
}
