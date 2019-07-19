package com.simibubi.mightyarchitect.control.phase;

import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.control.phase.export.PhaseListThemesForEditing;
import com.simibubi.mightyarchitect.control.phase.export.PhaseManageThemes;

public enum ArchitectPhases {

	/** No active architect process, don't do anything. */
	Empty(new PhaseEmpty(), "Choose a theme:"),
	
	/** Ground Plan is being drawn by the user. */
	Composing(new PhaseComposing(), "Compose Mode"),
	
	/** Preview the Schematic in its current state. */
	Previewing(new PhasePreviewing(), "Preview Mode"),
	
	/** Create a new Palette while previewing its effect. */
	CreatingPalette(new PhaseCreatingPalette(), "Pallete Mode"),
	
	// Creator phases
	
	ManagingThemes(new PhaseManageThemes(), "Manage Themes"),
	ListForEdit(new PhaseListThemesForEditing(), "Edit a Theme"),
	EditingThemes(new PhaseEditTheme(), "Editing "),
	PrintingToMultiplayer(new PrintingToMultiplayer(), "Printing Blocks...");
	
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
		if (this == ArchitectPhases.EditingThemes)
			 return displayTitle + DesignExporter.theme.getDisplayName();
		return displayTitle;
	}

}
