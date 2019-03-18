package com.simibubi.mightyarchitect.control.phase;

public enum ArchitectPhases {

	/** No active architect process, don't do anything. */
	Empty(new PhaseEmpty()),
	
	/** Ground Plan is being drawn by the user. */
	Composing(new PhaseComposing()),
	
	/** Preview the Schematic in its current state. */
	Previewing(new PhasePreviewing()),
	
	/** Create a new Palette while previewing its effect. */
	CreatingPalette(new PhaseCreatingPalette()),
	
	/** Modify the blocks in the world before writing it to the Schematic (WIP) */
	Editing(new PhaseEditing());
	
	private IArchitectPhase handler;
	
	private ArchitectPhases(IArchitectPhase handler) {
		this.handler = handler;
	}
	
	public IArchitectPhase getPhaseHandler() {
		return handler;
	}

}
