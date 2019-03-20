package com.simibubi.mightyarchitect.control.phase;

public enum ArchitectPhases {

	/** No active architect process, don't do anything. */
	Empty(new PhaseEmpty(), "Choose a theme:"),
	
	/** Ground Plan is being drawn by the user. */
	Composing(new PhaseComposing(), "Compose Mode"),
	
	/** Preview the Schematic in its current state. */
	Previewing(new PhasePreviewing(), "Preview Mode"),
	
	/** Create a new Palette while previewing its effect. */
	CreatingPalette(new PhaseCreatingPalette(), "Pallete Mode"),
	
	/** Modify the blocks in the world before writing it to the Schematic (WIP) */
	Editing(new PhaseEditing(), "");
	
	private IArchitectPhase handler;
	private String displayTitle;
	
	private ArchitectPhases(IArchitectPhase handler, String displayName) {
		this.handler = handler;
		this.displayTitle = displayName;
	}
	
	public IArchitectPhase getPhaseHandler() {
		return handler;
	}
	
	public String getDisplayTitle() {
		return displayTitle;
	}

}
