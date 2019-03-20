package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class PhaseEmpty extends PhaseBase {

	@Override
	public void whenEntered() {
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
	}
	
	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Themes affect the different designs used to decorate your Ground Plan.", "It does not Specify which blocks are used.");
	}

}
