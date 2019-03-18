package com.simibubi.mightyarchitect.buildomatico.phase;

import com.simibubi.mightyarchitect.buildomatico.client.SchematicHologram;

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

}
